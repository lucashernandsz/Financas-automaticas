// app/src/main/java/com/nate/autofinance/domain/usecases/transaction/AddTransactionUseCase.kt
package com.nate.autofinance.domain.usecases.transaction

import android.content.Context
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.utils.Categories
import com.nate.autofinance.utils.SessionManager

class AddTransactionUseCase(
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

        println("AddTransactionUseCase: Adicionando transação para período ${period.id}")

        val category = if (Categories.fixedCategories.contains(transaction.category))
            transaction.category
        else
            Categories.OTHER

        val amount = if (category != Categories.INCOME && transaction.amount > 0)
            -transaction.amount
        else
            transaction.amount

        val tx = transaction.copy(
            userId = userId,
            financialPeriodId = period.id,
            category = category,
            amount = amount,
            date = transaction.date,
            firebaseDocFinancialPeriodId = period.firebaseDocId
        )

        // Adiciona a transação
        transactionRepo.add(tx)

        // Garante que o período selecionado está correto no SessionManager
        session.saveSelectedPeriodId(context, period.id)

        println("AddTransactionUseCase: Transação adicionada com sucesso para período ${period.id}")
    }
}