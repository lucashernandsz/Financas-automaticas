package com.nate.autofinance.data.repository

import android.util.Log
import com.nate.autofinance.data.local.TransactionDao
import com.nate.autofinance.data.remote.FirebaseTransactionService
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val firebaseTransactionService: FirebaseTransactionService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val TAG = "TransactionRepository"
    }

    /**
     * Insere a transação no banco local e tenta enviá-la para o Firebase.
     * Caso a sincronização remota falhe, o erro é registrado para tratamento posterior.
     */
    suspend fun addTransaction(transaction: Transaction) = withContext(ioDispatcher) {
        // Persistência local
        transactionDao.insert(transaction)

        // Sincronização remota com o Firebase
        try {
            val firebaseDocId = firebaseTransactionService.addTransaction(transaction)
            Log.i(TAG, "Transação enviada para o Firebase com id: $firebaseDocId")
            // Se necessário, atualize a transação local com o firebaseDocId.
            // Exemplo: transaction.firebaseDocId = firebaseDocId
        } catch (ex: Exception) {
            Log.e(TAG, "Falha ao enviar transação para o Firebase", ex)
            // Marcar a transação para reenvio ou tratar de outra forma
        }
    }

    /**
     * Atualiza a transação local e, se disponível, atualiza a transação remota.
     * Para a atualização remota, é necessário que o modelo possua um identificador do documento Firebase.
     */
    suspend fun updateTransaction(transaction: Transaction) = withContext(ioDispatcher) {
        transactionDao.update(transaction)
        try {
            // Supondo que Transaction possua o campo firebaseDocId (opcional)
            transaction.firebaseDocId?.let { docId ->
                val updatedData: Map<String, Any?> = mapOf(
                    "date" to transaction.date,
                    "amount" to transaction.amount,
                    "description" to transaction.description,
                    "category" to transaction.category,
                    "userId" to transaction.userId,
                    "financialPeriodId" to transaction.financialPeriodId,
                    "imported" to transaction.imported
                )
                firebaseTransactionService.updateTransaction(docId, updatedData)
                Log.i(TAG, "Transação atualizada no Firebase: $docId")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Falha ao atualizar transação no Firebase", ex)
        }
    }

    /**
     * Remove a transação local e tenta removê-la do Firebase.
     */
    suspend fun deleteTransaction(transaction: Transaction) = withContext(ioDispatcher) {
        transactionDao.delete(transaction)
        try {
            // Supondo que Transaction possua o campo firebaseDocId (opcional)
            transaction.firebaseDocId?.let { docId ->
                firebaseTransactionService.deleteTransaction(docId)
                Log.i(TAG, "Transação deletada no Firebase: $docId")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Falha ao deletar transação no Firebase", ex)
        }
    }

    suspend fun getTransactionsByPeriodId(periodId: Int): List<Transaction> = withContext(ioDispatcher) {
        transactionDao.getTransactionByFinancialPeriodId(periodId)
    }
}
