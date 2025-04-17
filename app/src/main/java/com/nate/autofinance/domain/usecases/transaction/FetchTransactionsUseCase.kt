package com.nate.autofinance.domain.usecases.transaction

import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchTransactionsUseCase(private val transactionRepository: TransactionRepository) {

    suspend operator fun invoke(periodId: Int): List<Transaction> = withContext(Dispatchers.IO) {
        transactionRepository.getTransactionsByPeriodId(periodId)
    }
}
