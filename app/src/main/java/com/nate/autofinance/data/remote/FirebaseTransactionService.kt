package com.nate.autofinance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.tasks.await

/**
 * Serviço responsável pelas operações de persistência de transações no Firestore,
 * usando sempre o FirebaseAuth UID como identificador de usuário.
 */
class FirebaseTransactionService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val transactionsCollection = firestore.collection("transactions")

    /**
     * Adiciona uma nova transação ao Firestore e retorna o ID do documento criado.
     * Usa o Auth-UID em "firebaseDocUserId" e o remote periodId em "firebaseDocFinancialPeriodId".
     */
    suspend fun addTransaction(tx: Transaction): String {
        val authUid = FirebaseAuth.getInstance().currentUser
            ?.uid
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        val txMap = hashMapOf(
            "date"                         to tx.date,
            "amount"                       to tx.amount,
            "description"                  to tx.description,
            "category"                     to tx.category,
            "imported"                     to tx.imported,
            "syncStatus"                   to tx.syncStatus.name,
            "firebaseDocUserId"            to authUid,
            // usa o campo que já contém o docId do período remoto
            "firebaseDocFinancialPeriodId" to (tx.firebaseDocFinancialPeriodId ?: "")
        )

        val docRef = transactionsCollection.add(txMap).await()
        return docRef.id
    }

    /**
     * Atualiza campos específicos da transação existente.
     * @param documentId ID do documento no Firestore.
     * @param updatedData pares campo→valor a atualizar.
     */
    suspend fun updateTransaction(documentId: String, updatedData: Map<String, Any>) {
        transactionsCollection
            .document(documentId)
            .update(updatedData)
            .await()
    }

    /**
     * Remove a transação no Firestore dado o seu documentId.
     */
    suspend fun deleteTransaction(documentId: String) {
        transactionsCollection
            .document(documentId)
            .delete()
            .await()
    }

    /**
     * Busca todas as transações deste usuário (Auth-UID).
     */
    suspend fun getTransactionsForUser(): List<Transaction> {
        val authUid = FirebaseAuth.getInstance().currentUser
            ?.uid
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        return transactionsCollection
            .whereEqualTo("firebaseDocUserId", authUid)
            .get()
            .await()
            .map { it.toObject(Transaction::class.java) }
    }
}
