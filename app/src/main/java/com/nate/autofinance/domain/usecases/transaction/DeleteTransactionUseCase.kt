package com.nate.autofinance.domain.usecases.transaction

import android.content.Context
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    periodRepository: PeriodRepository,
    SessionManager: SessionManager,
    context: Context
) {

    suspend operator fun invoke(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionRepository.deleteTransaction(transaction)
    }
}
