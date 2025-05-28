package com.nate.autofinance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.domain.models.SyncStatus
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

    suspend fun updateTransaction(
        documentId: String,
        updatedData: Map<String, Any?>
    ) {
        txCollection
            .document(documentId)
            .set(updatedData, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    suspend fun deleteTransaction(documentId: String) {
        txCollection.document(documentId).delete().await()
    }

    /**
     * Mapeia manualmente cada DocumentSnapshot, convertendo Timestamp → Date
     * e capturando firebaseDocId e firebaseDocFinancialPeriodId.
     */
    suspend fun getTransactionsForUser(): List<Transaction> {
        val authUid = FirebaseAuth.getInstance().currentUser!!.uid
        val snaps = txCollection
            .whereEqualTo("firebaseDocUserId", authUid)
            .get()
            .await()

        return snaps.documents.map { doc ->
            // 1) data
            val timestamp = doc.getTimestamp("date")
                ?: throw IllegalStateException("Campo 'date' ausente no documento ${doc.id}")
            val date = timestamp.toDate()

            // 2) outros campos
            val amount      = doc.getDouble("amount") ?: 0.0
            val description = doc.getString("description") ?: ""
            val category    = doc.getString("category") ?: ""
            val imported    = doc.getBoolean("imported") ?: false
            val fpDocId     = doc.getString("firebaseDocFinancialPeriodId")

            Transaction(
                id                           = 0,
                date                         = date,
                amount                       = amount,
                description                  = description,
                category                     = category,
                userId                       = null,
                financialPeriodId            = 0,    // será ajustado no SyncManager
                imported                     = imported,
                syncStatus                   = SyncStatus.SYNCED,
                firebaseDocFinancialPeriodId = fpDocId,
                firebaseDocUserId            = authUid,
                firebaseDocId                = doc.id
            )
        }
    }
}
