package com.nate.autofinance.domain.usecase.period

import android.content.Context
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.utils.SessionManager
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.domain.models.FinancialPeriod

class GetCurrentActivePeriodUseCase(
    private val periodRepo: PeriodRepository = ServiceLocator.periodRepository,
    private val session: SessionManager = ServiceLocator.sessionManager,
    private val context: Context = ServiceLocator.context
) {
    suspend operator fun invoke(): FinancialPeriod {
        val userId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado para buscar o período ativo.")

        return periodRepo.getSelectedPeriodForUser(userId)
            ?: throw IllegalStateException("Nenhum período financeiro ativo encontrado para o usuário.")
    }
}