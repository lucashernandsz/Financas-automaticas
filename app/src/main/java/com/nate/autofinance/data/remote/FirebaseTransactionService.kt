package com.nate.autofinance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.domain.models.SyncStatus
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.tasks.await

class FirebaseTransactionService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val transactionsCollection = firestore.collection("transactions")

    // Adiciona uma nova transação na coleção e retorna o ID do documento criado.
    suspend fun addTransaction(transaction: Transaction): String? {
        val currentUid = FirebaseAuth.getInstance().currentUser
            ?.uid
            ?: throw IllegalStateException("Nenhum usuário autenticado")
        // Inclui o syncStatus no mapa (convertido para String)
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
        return transactionsCollection
            .whereEqualTo("firebaseDocUserId", firebaseUid)  // ← novo
            .get().await()
            .map { it.toObject(Transaction::class.java) }
    }
}
