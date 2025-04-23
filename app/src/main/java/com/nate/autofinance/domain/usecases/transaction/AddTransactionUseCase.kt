// app/src/main/java/com/nate/autofinance/domain/usecases/transaction/AddTransactionUseCase.kt
package com.nate.autofinance.domain.usecases.transaction

import android.content.Context
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.utils.Categories
import com.nate.autofinance.utils.SessionManager
import java.util.Date

class AddTransactionUseCase(
    private val transactionRepo: TransactionRepository,
    private val periodRepo: PeriodRepository,
    private val session: SessionManager,
    private val context: Context
) {
    suspend operator fun invoke(transaction: Transaction) {
        val userId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")                    // :contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1}

        val period = periodRepo.getSelectedPeriodForUser(userId)
            ?: throw IllegalStateException("Nenhum período ativo")                     // :contentReference[oaicite:2]{index=2}&#8203;:contentReference[oaicite:3]{index=3}

        val category = if (Categories.fixedCategories.contains(transaction.category))
            transaction.category
        else
            Categories.OTHER                                                            // :contentReference[oaicite:4]{index=4}&#8203;:contentReference[oaicite:5]{index=5}

        val amount = if (category != Categories.INCOME && transaction.amount > 0)
            -transaction.amount
        else
            transaction.amount

        val tx = transaction.copy(
            userId = userId,
            financialPeriodId = period.id,
            category = category,
            amount = amount,
            date = transaction.date
        )

        transactionRepo.addTransaction(tx)                                              // :contentReference[oaicite:6]{index=6}&#8203;:contentReference[oaicite:7]{index=7}
    }
}
