package com.nate.autofinance.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.tasks.await

class FirebaseTransactionService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val transactionsCollection = firestore.collection("transactions")

    // Adiciona uma nova transação na coleção e retorna o ID do documento criado.
    suspend fun addTransaction(transaction: Transaction): String? {
        val transactionMap = hashMapOf(
            "date" to transaction.date,
            "amount" to transaction.amount,
            "description" to transaction.description,
            "category" to transaction.category,
            "userId" to transaction.userId,
            "financialPeriodId" to transaction.financialPeriodId,
            "imported" to transaction.imported
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
    suspend fun getTransactionsForUser(userId: String): List<Transaction> {
        val querySnapshot = transactionsCollection.whereEqualTo("userId", userId).get().await()
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(Transaction::class.java)
        }
    }
}
