package com.nate.autofinance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.domain.models.FinancialPeriod
import kotlinx.coroutines.tasks.await

class FirebasePeriodService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val periodsCollection = firestore.collection("financialPeriods")

    suspend fun addFinancialPeriod(period: FinancialPeriod): String? {
        val currentUid = FirebaseAuth.getInstance().currentUser
            ?.uid
            ?: throw IllegalStateException("Nenhum usuário autenticado")
        val periodMap = hashMapOf(
            "startDate" to period.startDate,
            "endDate" to period.endDate,
            "isSelected" to period.isSelected,
            "totalIncome" to period.totalIncome,
            "totalExpenses" to period.totalExpenses,
            "syncStatus" to period.syncStatus.name,
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

    suspend fun getFinancialPeriodsForUser(documentId: String): List<FinancialPeriod> {
        return periodsCollection
            .whereEqualTo("firebaseDocUserId", documentId)
            .get().await()
            .map { it.toObject(FinancialPeriod::class.java) }
    }
}
