package com.nate.autofinance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.tasks.await

class FirebaseTransactionService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val txCollection = firestore.collection("transactions")

    suspend fun addTransaction(tx: Transaction): String {
        val authUid = FirebaseAuth.getInstance().currentUser!!.uid
        val txMap = hashMapOf(
            "date"                         to tx.date,
            "amount"                       to tx.amount,
            "description"                  to tx.description,
            "category"                     to tx.category,
            "imported"                     to tx.imported,
            "syncStatus"                   to tx.syncStatus.name,
            "firebaseDocUserId"            to authUid,
            "firebaseDocFinancialPeriodId" to (tx.firebaseDocFinancialPeriodId ?: "")
        )
        return txCollection.add(txMap).await().id
    }

    /**
     * Agora recebe Map<String, Any?> e faz set+merge.
     */
    suspend fun updateTransaction(
        documentId: String,
        updatedData: Map<String, Any?>
    ) {
        txCollection
            .document(documentId)
            .set(updatedData, SetOptions.merge())
            .await()
    }

    suspend fun deleteTransaction(documentId: String) {
        txCollection.document(documentId).delete().await()
    }

    suspend fun getTransactionsForUser(): List<Transaction> {
        val authUid = FirebaseAuth.getInstance().currentUser!!.uid
        return txCollection
            .whereEqualTo("firebaseDocUserId", authUid)
            .get()
            .await()
            .map { it.toObject(Transaction::class.java) }
    }
}
