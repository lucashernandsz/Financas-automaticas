package com.nate.autofinance.domain.usecases.transaction

import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.domain.models.Transaction

/**
 * Caso de uso para buscar uma única transação pelo seu ID.
 */
class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository
) {
    /**
     * Retorna a transação com o [id] informado, ou lança exceção se não existir.
     */
    suspend operator fun invoke(id: Int): Transaction {
        return transactionRepository.getTransactionById(id)
            ?: throw NoSuchElementException("Transação com id $id não encontrada")
    }
}
