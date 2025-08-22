// app/src/main/java/com/nate/autofinance/domain/usecases/transaction/EditTransactionUseCase.kt
package com.nate.autofinance.domain.usecase.transaction

import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.domain.usecase.period.GetCurrentActivePeriodUseCase
import com.nate.autofinance.utils.Categories
import com.nate.autofinance.utils.normalizeAmount

/**
 * Atualiza uma transação existente, reafirmando userId e financialPeriodId
 * para evitar inconsistências, e validando a categoria.
 */
class EditTransactionUseCase(
    private val transactionRepo: TransactionRepository,
) {
    suspend operator fun invoke(transaction: Transaction) {
        val period = GetCurrentActivePeriodUseCase().invoke()
        val category = Categories.find(transaction.category)
        val adjustedAmount = normalizeAmount(transaction.amount, category.name)

        val tx = transaction.copy(
            userId            = period.userId,
            financialPeriodId = period.id,
            category          = category.name,
            amount            = adjustedAmount,
        )

        transactionRepo.update(tx)
    }
}
