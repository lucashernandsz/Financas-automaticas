package com.nate.autofinance.domain.usecases

import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddTransactionUseCase(private val transactionRepository: TransactionRepository) {

    // Permite utilizar o use case como função
    suspend operator fun invoke(transaction: Transaction) = withContext(Dispatchers.IO) {
        // Chama o método do repositório para adicionar a transação
        transactionRepository.addTransaction(transaction)
    }
}
