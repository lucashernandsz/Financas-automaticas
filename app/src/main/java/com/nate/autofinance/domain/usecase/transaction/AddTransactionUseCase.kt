// app/src/main/java/com/nate/autofinance/domain/usecases/transaction/AddTransactionUseCase.kt
package com.nate.autofinance.domain.usecase.transaction

import android.content.Context
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.domain.models.User
import com.nate.autofinance.domain.usecase.period.GetCurrentActivePeriodUseCase
import com.nate.autofinance.utils.Categories
import com.nate.autofinance.utils.SessionManager
import com.nate.autofinance.utils.normalizeAmount
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration.Companion.days

class AddTransactionUseCase(
    private val transactionRepo: TransactionRepository,
    private val periodRepo: PeriodRepository,
    private val session: SessionManager,
    private val context: Context,
    private val getCurrentActivePeriod: GetCurrentActivePeriodUseCase,
) {
    suspend operator fun invoke(transaction: Transaction) {
        val category = Categories.find(transaction.category)
        val amount = normalizeAmount(transaction.amount, category.name)
        val userId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")

        val user = ServiceLocator.userRepository.getUserById(userId)
            ?: throw IllegalStateException("Usuário não encontrado no banco de dados")
        val period = if (transaction.isCredit){
            periodRepo.getNextPeriodForUser(userId = session.getUserId(context))
                ?: getCurrentActivePeriod()
        }else{
            getCurrentActivePeriod()
        }

        var tx = transaction.copy(
            userId = period.userId,
            financialPeriodId = period.id,
            category = category.name,
            amount = amount,
            date = transaction.date,
            firebaseDocFinancialPeriodId = period.firebaseDocId
        )

        if (transaction.isCredit) {

            val creditDate = getCreditDate(transaction.date, user.closingDate).time

            tx = tx.copy(description = "${transaction.description} (Crédito)", date = creditDate)
            if (transaction.numberOfInstallments >= 1){
                addInstallments(tx, user)
            }else{
                transactionRepo.add(tx)
            }
        }else{
            transactionRepo.add(tx)
        }

        session.saveSelectedPeriodId(context, period.id)
    }

    private fun getCreditDate(date: Date, closingDay: Int): Calendar {
        val cal = Calendar.getInstance()
        cal.time = date

        // Se a compra passou do dia de fechamento, joga para o mês seguinte
        if (cal.get(Calendar.DAY_OF_MONTH) > closingDay) {
            cal.add(Calendar.MONTH, 1)
        }

        // Ajusta para o último dia válido do mês alvo
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, minOf(closingDay, maxDay))

        return cal
    }


    suspend fun addInstallments(
        transaction: Transaction,
        user: User,
    ) {
        val transactions = mutableListOf<Transaction>()
        val amount = normalizeAmount(transaction.amount, Categories.find(transaction.category).name)
        val startDate = transaction.date
        val totalInstallments = transaction.numberOfInstallments
        val installmentAmount = String.format("%.2f", amount / totalInstallments).toDouble()
        val periods = periodRepo.getPeriodsForInstallments(user.id, totalInstallments)

        for (i in 0 until totalInstallments) {
            val installmentDate = Calendar.getInstance().apply {
                time = startDate
                add(Calendar.MONTH, i)
            }.time

            val period = if (i < periods.size) periods[i] else periods.last()
            val firebaseDocId = if (i < periods.size) periods[i].firebaseDocId else periods.last().firebaseDocId

            val installmentTransaction = transaction.copy(
                id = 0, // Novo ID para cada parcela
                amount = installmentAmount,
                date = installmentDate,
                description = "${transaction.description} (${i + 1}/$totalInstallments)",
                financialPeriodId = period.id,
                firebaseDocFinancialPeriodId = firebaseDocId
            )
            transactions.add(installmentTransaction)
        }

        transactions.forEach { transactionRepo.add(it) }
    }
}