// app/src/main/java/com/nate/autofinance/domain/usecases/transaction/AddTransactionUseCase.kt
package com.nate.autofinance.domain.usecase.transaction

import android.content.Context
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.domain.usecase.period.GetCurrentActivePeriodUseCase
import com.nate.autofinance.utils.Categories
import com.nate.autofinance.utils.SessionManager
import com.nate.autofinance.utils.normalizeAmount

class AddTransactionUseCase(
    private val transactionRepo: TransactionRepository,
    private val session: SessionManager,
    private val context: Context,
    private val getCurrentActivePeriod: GetCurrentActivePeriodUseCase
) {
    suspend operator fun invoke(transaction: Transaction) {
        val period = getCurrentActivePeriod()
        val category = Categories.find(transaction.category)
        val amount = normalizeAmount(transaction.amount, category.name)

        val tx = transaction.copy(
            userId = period.id,
            financialPeriodId = period.id,
            category = category.name,
            amount = amount,
            date = transaction.date,
            firebaseDocFinancialPeriodId = period.firebaseDocId
        )

        transactionRepo.add(tx)
        session.saveSelectedPeriodId(context, period.id)
    }
}