// app/src/main/java/com/nate/autofinance/domain/usecases/transaction/EditTransactionUseCase.kt
package com.nate.autofinance.domain.usecases.transaction

import android.content.Context
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.utils.Categories
import com.nate.autofinance.utils.SessionManager

/**
 * Atualiza uma transação existente, reafirmando userId e financialPeriodId
 * para evitar inconsistências, e validando a categoria.
 */
class EditTransactionUseCase(
    private val transactionRepo: TransactionRepository,
    private val periodRepo: PeriodRepository,
    private val session: SessionManager,
    private val context: Context
) {
    suspend operator fun invoke(transaction: Transaction) {
        val userId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")

        val period = periodRepo.getSelectedPeriodForUser(userId)
            ?: throw IllegalStateException("Nenhum período ativo")

        val category = if (Categories.fixedCategories.contains(transaction.category))
            transaction.category
        else
            Categories.OTHER

        val tx = transaction.copy(
            userId            = userId,
            financialPeriodId = period.id,
            category          = category
        )

        transactionRepo.updateTransaction(tx)                                           // :contentReference[oaicite:8]{index=8}&#8203;:contentReference[oaicite:9]{index=9}
    }
}
