// TransactionRepository.kt
package com.nate.autofinance.data.repository

import com.nate.autofinance.data.local.TransactionDao
import com.nate.autofinance.data.remote.FirebaseTransactionService
import com.nate.autofinance.data.models.Transaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val firebaseTransactionService: FirebaseTransactionService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val TAG = "TransactionRepository"
    }

    suspend fun add(transaction: Transaction) = withContext(ioDispatcher) {
        transactionDao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) = withContext(ioDispatcher) {
        transactionDao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) = withContext(ioDispatcher) {
        transactionDao.delete(transaction)
    }

    suspend fun getTransactionsByPeriodId(periodId: Int): List<Transaction> = withContext(ioDispatcher) {
        val transactions = transactionDao.getTransactionsByFinancialPeriodId(periodId)
        return@withContext transactions
    }

    suspend fun getTransactionById(id: Int): Transaction? = withContext(ioDispatcher) {
        transactionDao.getTransactionById(id)
    }

    suspend fun fetchRemoteTransactions(): List<Transaction> = withContext(ioDispatcher) {
        firebaseTransactionService.getTransactionsForUser()
    }

    fun observeTransactions(periodId: Int): Flow<List<Transaction>> {
        return transactionDao.observeTransactionsByPeriodId(periodId)
            .catch   { exception ->
                Timber.tag(TAG).e(exception, "Erro no fluxo de transações para período $periodId")
            }
    }

    fun observeTransactionsByUser(userId: Int): Flow<List<Transaction>> {
        return transactionDao.observeTransactionsByUserId(userId)
            .catch { exception ->
                Timber.tag(TAG).e(exception, "Erro no fluxo de transações para usuário $userId")
            }
    }

    fun observePendingTransactions(): Flow<List<Transaction>> {
        return transactionDao.observePendingTransactions()
            .onEach { transactions ->
                println("$TAG: ✅ ${transactions.size} transações pendentes de sincronização")
            }
    }
}