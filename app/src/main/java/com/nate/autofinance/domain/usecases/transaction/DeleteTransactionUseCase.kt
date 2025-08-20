package com.nate.autofinance.domain.usecases.transaction

import android.content.Context
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val sessionManager: SessionManager,
    private val context: Context
) {
    suspend operator fun invoke(transaction: Transaction) = withContext(Dispatchers.IO) {
        val userId = sessionManager.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")
        // Opcional: verificar se a transação pertence ao usuário
        transactionRepository.delete(transaction)
    }
}
