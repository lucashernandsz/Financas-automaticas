package com.nate.autofinance.domain.usecases.period

import android.content.Context
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.data.models.FinancialPeriod
import com.nate.autofinance.utils.SessionManager

class DeletePeriodsUseCase(
    private val periodRepository: PeriodRepository,
    private val sessionManager: SessionManager,
    private val context: Context
) {
    suspend operator fun invoke(periods: List<FinancialPeriod>) {
        // Opcional: validar usuário logado antes de excluir
        sessionManager.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")

        val selected = periods.firstOrNull { it.isSelected }
        if (selected != null) {
            throw IllegalStateException("Não é permitido excluir um período financeiro selecionado.")
        }

        periods.forEach { period ->
            periodRepository.delete(period)
        }
    }
}
