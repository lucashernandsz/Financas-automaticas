package com.nate.autofinance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.domain.models.FinancialPeriod
import com.nate.autofinance.domain.models.SyncStatus
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebasePeriodService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val periodsCollection = firestore.collection("financialPeriods")

    suspend fun addFinancialPeriod(period: FinancialPeriod): String? {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val periodMap = hashMapOf(
            "startDate" to period.startDate,
            "endDate" to period.endDate,
            "isSelected" to period.isSelected,
            "totalIncome" to period.totalIncome,
            "totalExpenses" to period.totalExpenses,
            "syncStatus" to SyncStatus.SYNCED.name,
            "userId" to period.userId,
            "firebaseDocUserId" to currentUid,
        )
        val documentRef = periodsCollection.add(periodMap).await()
        return documentRef.id
    }

    suspend fun updateFinancialPeriod(documentId: String, updatedData: Map<String, Any>) {
        periodsCollection.document(documentId).update(updatedData).await()
    }

    suspend fun deleteFinancialPeriod(documentId: String) {
        periodsCollection.document(documentId).delete().await()
    }

    suspend fun getFinancialPeriodsForUser(uid: String): List<FinancialPeriod> {
        val snaps = periodsCollection
            .whereEqualTo("firebaseDocUserId", uid)
            .get().await()
            .documents

        return snaps.mapNotNull { snap ->
            // 1) Datas
            val tsStart = snap.getTimestamp("startDate")
            val tsEnd   = snap.getTimestamp("endDate")
            if (tsStart == null || tsEnd == null) return@mapNotNull null
            val startDate: Date = tsStart.toDate()
            val endDate:   Date = tsEnd.toDate()

            // 2) Outros campos
            val totalIncome   = (snap.getDouble("totalIncome") ?: 0.0)
            val totalExpenses = (snap.getDouble("totalExpenses") ?: 0.0)
            val isSelected    = snap.getBoolean("isSelected") ?: false
            val rawStatus     = snap.getString("syncStatus") ?: SyncStatus.PENDING.name
            val syncStatus    = try {
                SyncStatus.valueOf(rawStatus)
            } catch (_: Exception) {
                SyncStatus.PENDING
            }

            // 3) userId local (salvo como número no documento)
            val userIdNum = snap.getLong("userId")?.toInt()
                ?: return@mapNotNull null

            // 4) Constroi a entidade
            FinancialPeriod(
                id                   = 0,           // será gerado pelo Room
                startDate            = startDate,
                endDate              = endDate,
                totalIncome          = totalIncome,
                totalExpenses        = totalExpenses,
                userId               = userIdNum,   // pega do campo "userId"
                isSelected           = isSelected,
                syncStatus           = syncStatus,
                firebaseDocId        = snap.id,
                firebaseDocUserId    = uid
            )
        }
    }


}
