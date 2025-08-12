package com.nate.autofinance.domain.usecase.transaction

import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionRepository.delete(transaction)
    }
}
