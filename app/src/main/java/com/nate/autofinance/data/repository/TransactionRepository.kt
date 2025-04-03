package com.nate.autofinance.data.repository

import com.nate.autofinance.data.local.TransactionDao
import com.nate.autofinance.domain.models.Transaction

class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
        // Aqui você pode incluir a lógica para sincronização com o Firebase, se necessário.
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
        // Adicionar lógica para sincronização remota, se aplicável.
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
        // Implementar sincronização para remoção na fonte remota, se necessário.
    }

    suspend fun getTransactionsByPeriodId(periodId: Int): List<Transaction> {
        return transactionDao.getTransactionByFinancialPeriodId(periodId)
    }
}
