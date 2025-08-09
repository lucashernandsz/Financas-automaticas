package com.nate.autofinance.domain.usecases.transaction

import android.content.Context
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.data.models.Transaction
import com.nate.autofinance.utils.SessionManager

class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository,
    private val sessionManager: SessionManager,
    private val context: Context
) {
    suspend operator fun invoke(id: Int): Transaction {
        sessionManager.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")
        return transactionRepository.getTransactionById(id)
            ?: throw NoSuchElementException("Transação com id $id não encontrada")
    }
}