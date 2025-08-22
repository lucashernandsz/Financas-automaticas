package com.nate.autofinance.domain.usecases.period

import android.content.Context
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.data.repository.UserRepository
import com.nate.autofinance.domain.models.FinancialPeriod
import com.nate.autofinance.utils.SessionManager
import com.nate.autofinance.utils.toDate
import java.time.LocalDate

class CreatePredefinedPeriodsUseCase(
    private val periodRepository: PeriodRepository,
    private val session: SessionManager,
    private val context: Context
) {
    suspend operator fun invoke() {
        val userId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")

        val closingDay = 1

        if (periodRepository.getFinancialPeriodsForUser(userId).isNotEmpty()) {
            return
        }

        val periodsToCreate = mutableListOf<FinancialPeriod>()
        val today = LocalDate.now()

        // Calcula a data de término do primeiro período
        var periodEndDate = today.withDayOfMonth(closingDay)
        if (today.dayOfMonth > closingDay) {
            periodEndDate = periodEndDate.plusMonths(1)
        }

        for (i in 0 until 12) {
            val currentEndDate = periodEndDate.plusMonths(i.toLong())
            val currentStartDate = currentEndDate.minusMonths(1).plusDays(1)

            periodsToCreate.add(
                FinancialPeriod(
                    startDate = currentStartDate.toDate(),
                    endDate = currentEndDate.toDate(),
                    isSelected = (i == 0),
                    userId = userId
                )
            )
        }

        periodRepository.insertAll(periodsToCreate)

        // Salva o período selecionado na sessão
        val selectedPeriod = periodRepository.getSelectedPeriodForUser(userId)
        selectedPeriod?.let {
            session.saveSelectedPeriodId(context, it.id)
        }
    }
}