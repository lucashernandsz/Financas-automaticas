// TransactionRepository.kt
package com.nate.autofinance.data.repository

import android.util.Log
import com.nate.autofinance.data.local.TransactionDao
import com.nate.autofinance.data.remote.FirebaseTransactionService
import com.nate.autofinance.domain.models.SyncStatus
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val firebaseTransactionService: FirebaseTransactionService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val TAG = "TransactionRepository"
    }

    /** Insere no Room e tenta enviar ao Firebase, marcando status adequado. */
    suspend fun addTransaction(transaction: Transaction) = withContext(ioDispatcher) {
        println("$TAG: Inserindo transação: ${transaction.description} para período ${transaction.financialPeriodId}")

        val localId = transactionDao.insert(transaction)
        println("$TAG: Transação inserida no Room com ID: $localId")

        try {
            val firebaseDocId = firebaseTransactionService.addTransaction(transaction)
            if (firebaseDocId != null) {
                Log.i(TAG, "Transaction sent to Firebase with id: $firebaseDocId")
                val updated = transaction.copy(
                    id = localId.toInt(),
                    firebaseDocId = firebaseDocId,
                    syncStatus = SyncStatus.SYNCED
                )
                transactionDao.update(updated)
                println("$TAG: Transação sincronizada com Firebase: $firebaseDocId")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error sending transaction to Firebase", ex)
            val failed = transaction.copy(id = localId.toInt(), syncStatus = SyncStatus.FAILED)
            transactionDao.update(failed)
            println("$TAG: Erro ao sincronizar com Firebase: ${ex.message}")
        }
    }

    /** Atualiza local e remoto (se já tiver firebaseDocId). */
    suspend fun updateTransaction(transaction: Transaction) = withContext(ioDispatcher) {
        transactionDao.update(transaction)
        try {
            transaction.firebaseDocId?.let { docId ->
                val data = mapOf(
                    "date" to transaction.date,
                    "amount" to transaction.amount,
                    "description" to transaction.description,
                    "category" to transaction.category,
                    "userId" to transaction.userId,
                    "financialPeriodId" to transaction.financialPeriodId,
                    "imported" to transaction.imported,
                    "syncStatus" to transaction.syncStatus.name
                )
                firebaseTransactionService.updateTransaction(docId, data as Map<String, Any>)
                Log.i(TAG, "Transaction updated in Firebase: $docId")
            }
            val synced = transaction.copy(syncStatus = SyncStatus.SYNCED)
            transactionDao.update(synced)
        } catch (ex: Exception) {
            Log.e(TAG, "Error updating transaction in Firebase", ex)
            val failed = transaction.copy(syncStatus = SyncStatus.FAILED)
            transactionDao.update(failed)
        }
    }

    /** Exclui localmente e (tenta) excluir no Firebase. */
    suspend fun deleteTransaction(transaction: Transaction) = withContext(ioDispatcher) {
        transactionDao.delete(transaction)
        try {
            transaction.firebaseDocId?.let { firebaseTransactionService.deleteTransaction(it) }
            Log.i(TAG, "Transaction deleted from Firebase: ${transaction.firebaseDocId}")
        } catch (ex: Exception) {
            Log.e(TAG, "Error deleting transaction from Firebase", ex)
        }
    }

    /** Query direta (síncrona) para uso pontual. */
    suspend fun getTransactionsByPeriodId(periodId: Int): List<Transaction> = withContext(ioDispatcher) {
        val transactions = transactionDao.getTransactionsByFinancialPeriodId(periodId)
        println("$TAG: Buscando transações para período $periodId: ${transactions.size} encontradas")
        return@withContext transactions
    }

    /** Busca transação por ID. */
    suspend fun getTransactionById(id: Int): Transaction? = withContext(ioDispatcher) {
        transactionDao.getTransactionById(id)
    }

    /** Baixa todas as transações remotas para sync. */
    suspend fun fetchRemoteTransactions(): List<Transaction> = withContext(ioDispatcher) {
        firebaseTransactionService.getTransactionsForUser()
    }

    /** **Fluxo reativo**: emite nova lista sempre que o Room detectar mudanças. */
    fun observeTransactions(periodId: Int): Flow<List<Transaction>> {
        println("$TAG: Iniciando observação de transações para período $periodId")
        return transactionDao.observeTransactionsByPeriodId(periodId)
            .onEach { transactions ->
                println("$TAG: Fluxo emitiu ${transactions.size} transações para período $periodId")
            }
    }
}