package com.nate.autofinance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.data.models.FinancialPeriod
import com.nate.autofinance.data.models.SyncStatus
import kotlinx.coroutines.tasks.await

class FirebasePeriodService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val periodsCollection = firestore.collection("financialPeriods")

    suspend fun add(period: FinancialPeriod): String {
        val authUid = FirebaseAuth.getInstance().currentUser!!.uid
        val periodMap = hashMapOf(
            "startDate"         to period.startDate,
            "endDate"           to period.endDate,
            "isSelected"        to period.isSelected,
            "totalIncome"       to period.totalIncome,
            "totalExpenses"     to period.totalExpenses,
            "syncStatus"        to period.syncStatus.name,
            "firebaseDocUserId" to authUid
        )
        return periodsCollection.add(periodMap).await().id
    }

    suspend fun update(
        documentId: String,
        updatedData: Map<String, Any?>
    ) {
        periodsCollection
            .document(documentId)
            .set(updatedData, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    suspend fun delete(documentId: String) {
        periodsCollection.document(documentId).delete().await()
    }

    /**
     * Converte cada Timestamp de startDate/endDate para Date,
     * mantendo exatamente o que está no Firestore.
     */
    suspend fun getFinancialPeriodsForUser(): List<FinancialPeriod> {
        val authUid = FirebaseAuth.getInstance().currentUser!!.uid
        val snaps = periodsCollection
            .whereEqualTo("firebaseDocUserId", authUid)
            .get()
            .await()

        return snaps.documents.map { doc ->
            val startTs = doc.getTimestamp("startDate")
                ?: throw IllegalStateException("Campo 'startDate' ausente no documento ${doc.id}")
            val endTs   = doc.getTimestamp("endDate") // pode ser nulo

            FinancialPeriod(
                id                = 0,  //Room gera
                startDate         = startTs.toDate(),
                endDate           = endTs?.toDate(),
                isSelected        = doc.getBoolean("isSelected") ?: false,
                totalIncome       = doc.getDouble("totalIncome") ?: 0.0,
                totalExpenses     = doc.getDouble("totalExpenses") ?: 0.0,
                userId            = null,  // será preenchido no SyncManager
                syncStatus        = SyncStatus.SYNCED,
                firebaseDocUserId = authUid,
                firebaseDocId     = doc.id
            )
        }
    }
}
