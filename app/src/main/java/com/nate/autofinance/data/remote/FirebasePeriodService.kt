package com.nate.autofinance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nate.autofinance.domain.models.FinancialPeriod
import kotlinx.coroutines.tasks.await

class FirebasePeriodService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val periodsCollection = firestore.collection("financialPeriods")

    suspend fun addFinancialPeriod(period: FinancialPeriod): String {
        val authUid = FirebaseAuth.getInstance().currentUser!!.uid
        val periodMap = hashMapOf(
            "startDate"          to period.startDate,
            "endDate"            to period.endDate,
            "isSelected"         to period.isSelected,
            "totalIncome"        to period.totalIncome,
            "totalExpenses"      to period.totalExpenses,
            "syncStatus"         to period.syncStatus.name,
            "firebaseDocUserId"  to authUid
        )
        return periodsCollection.add(periodMap).await().id
    }

    /**
     * Agora recebe Map<String, Any?> e faz set+merge,
     * eliminando o conflito de Map<K,V> vs Map<String,Any>.
     */
    suspend fun updateFinancialPeriod(
        documentId: String,
        updatedData: Map<String, Any?>
    ) {
        periodsCollection
            .document(documentId)
            .set(updatedData, SetOptions.merge())
            .await()
    }

    suspend fun deleteFinancialPeriod(documentId: String) {
        periodsCollection.document(documentId).delete().await()
    }

    suspend fun getFinancialPeriodsForUser(): List<FinancialPeriod> {
        val authUid = FirebaseAuth.getInstance().currentUser!!.uid
        return periodsCollection
            .whereEqualTo("firebaseDocUserId", authUid)
            .get()
            .await()
            .map { it.toObject(FinancialPeriod::class.java) }
    }
}
