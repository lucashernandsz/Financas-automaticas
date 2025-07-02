package com.nate.autofinance.data.sync

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.data.local.FinancialPeriodDao
import com.nate.autofinance.data.local.TransactionDao
import com.nate.autofinance.domain.models.FinancialPeriod
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.domain.models.SyncStatus
import com.nate.autofinance.utils.SessionManager
import kotlinx.coroutines.tasks.await

class SyncManager(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val periodDao: FinancialPeriodDao,
    private val txDao: TransactionDao,
    private val session: SessionManager,
    private val context: Context
) {

    suspend fun syncAll() {
        // --- 1) Identifica o usuário autenticado e seu ID local
        val firebaseUser = FirebaseAuth.getInstance().currentUser
            ?: throw IllegalStateException("Nenhum usuário autenticado")
        val authUid = firebaseUser.uid
        val localUserId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário local definido")

        // --- 2) PUSH: períodos pendentes → Firestore
        periodDao.getPendingPeriods().forEach { period ->
            try {
                val docRef = if (period.firebaseDocId.isNullOrBlank()) {
                    // cria novo
                    firestore.collection("financialPeriods")
                        .add(mapOf(
                            "startDate"         to period.startDate,
                            "endDate"           to period.endDate,
                            "isSelected"        to period.isSelected,
                            "totalIncome"       to period.totalIncome,
                            "totalExpenses"     to period.totalExpenses,
                            "firebaseDocUserId" to authUid,
                            "syncStatus"        to period.syncStatus.name
                        ))
                        .await()
                } else {
                    // atualiza existente
                    firestore.collection("financialPeriods")
                        .document(period.firebaseDocId!!)
                        .set(mapOf(
                            "startDate"         to period.startDate,
                            "endDate"           to period.endDate,
                            "isSelected"        to period.isSelected,
                            "totalIncome"       to period.totalIncome,
                            "totalExpenses"     to period.totalExpenses,
                            "syncStatus"        to period.syncStatus.name
                        ), com.google.firebase.firestore.SetOptions.merge())
                        .await().let { null }  // não precisamos do retorno
                }

                // atualiza o status local
                periodDao.update(
                    period.copy(
                        firebaseDocId = docRef?.id ?: period.firebaseDocId,
                        syncStatus    = SyncStatus.SYNCED
                    )
                )
            } catch (e: Exception) {
                periodDao.update(period.copy(syncStatus = SyncStatus.FAILED))
            }
        }

        // --- 3) PUSH: transações pendentes → Firestore
        txDao.getPendingTransactions().forEach { tx ->
            try {
                val docRef = if (tx.firebaseDocId.isNullOrBlank()) {
                    firestore.collection("transactions")
                        .add(mapOf(
                            "date"                          to tx.date,
                            "amount"                        to tx.amount,
                            "description"                   to tx.description,
                            "category"                      to tx.category,
                            "imported"                      to tx.imported,
                            "firebaseDocUserId"             to authUid,
                            "firebaseDocFinancialPeriodId"  to tx.firebaseDocFinancialPeriodId,
                            "syncStatus"                    to tx.syncStatus.name
                        ))
                        .await()
                } else {
                    firestore.collection("transactions")
                        .document(tx.firebaseDocId!!)
                        .set(mapOf(
                            "date"                          to tx.date,
                            "amount"                        to tx.amount,
                            "description"                   to tx.description,
                            "category"                      to tx.category,
                            "imported"                      to tx.imported,
                            "syncStatus"                    to tx.syncStatus.name
                        ), com.google.firebase.firestore.SetOptions.merge())
                        .await().let { null }
                }

                txDao.update(
                    tx.copy(
                        firebaseDocId = docRef?.id ?: tx.firebaseDocId,
                        syncStatus    = SyncStatus.SYNCED
                    )
                )
            } catch (e: Exception) {
                txDao.update(tx.copy(syncStatus = SyncStatus.FAILED))
            }
        }

        // --- 4) PULL: baixa períodos remotos e insere no Room
        val periodSnaps = firestore.collection("financialPeriods")
            .whereEqualTo("firebaseDocUserId", authUid)
            .get().await()
        val remotePeriods = periodSnaps.documents.map { doc ->
            FinancialPeriod(
                id                = 0,
                startDate         = doc.getTimestamp("startDate")?.toDate(),
                endDate           = doc.getTimestamp("endDate")?.toDate(),
                isSelected        = doc.getBoolean("isSelected") ?: false,
                totalIncome       = doc.getDouble("totalIncome") ?: 0.0,
                totalExpenses     = doc.getDouble("totalExpenses") ?: 0.0,
                userId            = localUserId,
                syncStatus        = SyncStatus.SYNCED,
                firebaseDocUserId = authUid,
                firebaseDocId     = doc.id
            )
        }
        // PULL: upsert remote financial periods to avoid duplicates
        remotePeriods.forEach { period ->
            val existing = periodDao.getPeriodByFirebaseDocId(period.firebaseDocId!!)
            if (existing != null) {
                periodDao.update(period.copy(id = existing.id))
            } else {
                periodDao.insert(period.copy(id = 0))
            }
        }

        // --- 5) PULL: baixa transações remotas; vincula ao período local
        val txSnaps = firestore.collection("transactions")
            .whereEqualTo("firebaseDocUserId", authUid)
            .get().await()
        val remoteTxs = txSnaps.documents.mapNotNull { doc ->
             val parentId = doc.getString("firebaseDocFinancialPeriodId") ?: return@mapNotNull null
             val localPeriod = periodDao.getPeriodByFirebaseDocId(parentId)
                 ?: return@mapNotNull null

             Transaction(
                 id                           = 0,
                 date                         = doc.getTimestamp("date")!!.toDate(),
                 amount                       = doc.getDouble("amount") ?: 0.0,
                 description                  = doc.getString("description") ?: "",
                 category                     = doc.getString("category") ?: "",
                 userId                       = localUserId,
                 financialPeriodId            = localPeriod.id,
                 imported                     = doc.getBoolean("imported") ?: false,
                 syncStatus                   = SyncStatus.SYNCED,
                 firebaseDocFinancialPeriodId = parentId,
                 firebaseDocUserId            = authUid,
                 firebaseDocId                = doc.id
             )
         }
        // PULL: só insere novos e atualiza existentes para evitar duplicação
        remoteTxs.forEach { tx ->
            val docId = tx.firebaseDocId!!
            val existing = txDao.getTransactionByFirebaseDocId(docId)
            if (existing != null) {
                // atualiza dados mantendo o mesmo ID
                txDao.update(tx.copy(id = existing.id))
            } else {
                // insere novo, id auto gerado
                txDao.insert(tx.copy(id = 0))
            }
        }
    }
}
