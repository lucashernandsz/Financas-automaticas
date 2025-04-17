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
     * Após a inserção remota, atualiza o registro local com o firebaseDocId gerado e o syncStatus.
     */
    suspend fun addTransaction(transaction: Transaction) = withContext(ioDispatcher) {
        // 1. Insere a transação localmente e captura o ID gerado pelo Room.
        val localId = transactionDao.insert(transaction)

        try {
            // 2. Envia a transação para o Firebase e captura o firebaseDocId gerado.
            val firebaseDocId = firebaseTransactionService.addTransaction(transaction)
            // 3. Caso o firebaseDocId não seja nulo, atualiza o objeto local com status SYNCED.
            if (firebaseDocId != null) {
                Log.i(TAG, "Transaction sent to Firebase with id: $firebaseDocId")
                val updatedTransaction = transaction.copy(
                    id = localId.toInt(),          // Garante que o ID local seja o mesmo
                    firebaseDocId = firebaseDocId, // Atualiza o campo firebaseDocId
                    syncStatus = SyncStatus.SYNCED // Marca como sincronizada
                )
                transactionDao.update(updatedTransaction)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error sending transaction to Firebase", ex)
            // Se ocorrer erro, marca a transação local para nova tentativa de sincronização.
            val updatedTransaction = transaction.copy(
                id = localId.toInt(),
                syncStatus = SyncStatus.FAILED
            )
            transactionDao.update(updatedTransaction)
        }
    }

    /**
     * Atualiza a transação local e, se o firebaseDocId estiver presente,
     * atualiza também o registro remoto no Firebase.
     */
    suspend fun updateTransaction(transaction: Transaction) = withContext(ioDispatcher) {
        // Atualiza primeiro a transação local.
        transactionDao.update(transaction)
        try {
            transaction.firebaseDocId?.let { docId ->
                val updatedData: Map<String, Any?> = mapOf(
                    "date" to transaction.date,
                    "amount" to transaction.amount,
                    "description" to transaction.description,
                    "category" to transaction.category,
                    "userId" to transaction.userId,
                    "financialPeriodId" to transaction.financialPeriodId,
                    "imported" to transaction.imported,
                    "syncStatus" to transaction.syncStatus.name
                )
                firebaseTransactionService.updateTransaction(docId, updatedData as Map<String, Any>)
                Log.i(TAG, "Transaction updated in Firebase: $docId")
            }
            // Se a operação remota tiver sucesso, atualiza o syncStatus para SYNCED localmente.
            val updatedTransaction = transaction.copy(syncStatus = SyncStatus.SYNCED)
            transactionDao.update(updatedTransaction)
        } catch (ex: Exception) {
            Log.e(TAG, "Error updating transaction in Firebase", ex)
            // Em caso de erro, marca a transação como FAILED.
            val updatedTransaction = transaction.copy(syncStatus = SyncStatus.FAILED)
            transactionDao.update(updatedTransaction)
        }
    }

    /**
     * Remove a transação localmente e tenta removê-la do Firebase.
     */
    suspend fun deleteTransaction(transaction: Transaction) = withContext(ioDispatcher) {
        transactionDao.delete(transaction)
        try {
            transaction.firebaseDocId?.let { docId ->
                firebaseTransactionService.deleteTransaction(docId)
                Log.i(TAG, "Transaction deleted from Firebase: $docId")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error deleting transaction from Firebase", ex)
        }
    }

    suspend fun getTransactionsByPeriodId(periodId: Int): List<Transaction> = withContext(ioDispatcher) {
        transactionDao.getTransactionsByFinancialPeriodId(periodId)
    }
}
