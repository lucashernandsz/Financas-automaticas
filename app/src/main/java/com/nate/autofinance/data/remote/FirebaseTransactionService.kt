package com.nate.autofinance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.domain.models.SyncStatus
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseTransactionService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val transactionsCollection = firestore.collection("transactions")

    // Adiciona uma nova transação na coleção e retorna o ID do documento criado.
    suspend fun addTransaction(transaction: Transaction): String? {
        val currentUid = FirebaseAuth.getInstance().currentUser
            ?.uid ?: throw IllegalStateException("Nenhum usuário autenticado")

        val transactionMap = hashMapOf(
            "date" to transaction.date,
            "amount" to transaction.amount,
            "description" to transaction.description,
            "category" to transaction.category,
            "userId" to transaction.userId,
            "financialPeriodId" to transaction.financialPeriodId,
            "imported" to transaction.imported,
            "syncStatus" to SyncStatus.SYNCED.name,
            "firebaseDocFinancialPeriodId" to transaction.firebaseDocFinancialPeriodId,
            "firebaseDocUserId" to currentUid,
        )
        val documentRef = transactionsCollection.add(transactionMap).await()
        return documentRef.id
    }

    // Atualiza os campos de uma transação existente a partir de um map com os dados atualizados.
    suspend fun updateTransaction(transactionId: String, updatedData: Map<String, Any>) {
        transactionsCollection.document(transactionId).update(updatedData).await()
    }

    // Exclui uma transação com base no ID do documento.
    suspend fun deleteTransaction(transactionId: String) {
        transactionsCollection.document(transactionId).delete().await()
    }

    // Busca e retorna a lista de transações associadas a um determinado usuário.
    suspend fun getTransactionsForUser(firebaseUid: String): List<Transaction> {
        // 1) Busca os snaps
        val snaps = transactionsCollection
            .whereEqualTo("firebaseDocUserId", firebaseUid)
            .get()
            .await()
            .documents

        // 2) Converte cada DocumentSnapshot em Transaction
        return snaps.mapNotNull { snap ->
            // Data (Timestamp → Date)
            val ts = snap.getTimestamp("date") ?: return@mapNotNull null
            val date: Date = ts.toDate()

            // Campos numéricos e booleanos
            val amount: Double = snap.getDouble("amount") ?: return@mapNotNull null
            val imported: Boolean = snap.getBoolean("imported") ?: false

            // Strings
            val description: String = snap.getString("description") ?: ""
            val category:    String = snap.getString("category")    ?: ""

            // IDs (Long → Int ou nulo)
            val userIdNum: Int? = snap.getLong("userId")?.toInt()
            val periodId:  Int  = snap.getLong("financialPeriodId")
                ?.toInt() ?: return@mapNotNull null

            // Status de sincronização
            val rawStatus: String = snap.getString("syncStatus")
                ?: SyncStatus.PENDING.name
            val syncStatus: SyncStatus = try {
                SyncStatus.valueOf(rawStatus)
            } catch (_: Exception) {
                SyncStatus.PENDING
            }

            // IDs remotos
            val firebaseDocFinancialPeriodId: String? =
                snap.getString("firebaseDocFinancialPeriodId")
            val firebaseDocUserId: String? =
                snap.getString("firebaseDocUserId")

            // 3) Monta e retorna a Transaction
            Transaction(
                id                         = 0,  // Room vai gerar ou preservar localmente
                date                       = date,
                amount                     = amount,
                description                = description,
                category                   = category,
                userId                     = userIdNum,
                financialPeriodId          = periodId,
                imported                   = imported,
                syncStatus                 = syncStatus,
                firebaseDocFinancialPeriodId = firebaseDocFinancialPeriodId,
                firebaseDocUserId          = firebaseDocUserId,
                firebaseDocId              = snap.id
            )
        }
    }
}
