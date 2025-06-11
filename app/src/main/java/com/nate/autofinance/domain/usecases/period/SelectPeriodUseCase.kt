package com.nate.autofinance.domain.usecases.period

import android.content.Context
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.utils.SessionManager

class SelectPeriodUseCase(
    private val periodRepository: PeriodRepository,
    private val session: SessionManager,
    private val context: Context
) {
    suspend operator fun invoke(periodId: Int) {
        val userId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")

        val all = periodRepository.getPeriodsForUser(userId)
        all.forEach {
            val updated = it.copy(isSelected = (it.id == periodId))
            periodRepository.updateFinancialPeriod(updated)
        }

        // Atualiza o SessionManager para notificar observers
        session.saveSelectedPeriodId(context, periodId)
    }
}