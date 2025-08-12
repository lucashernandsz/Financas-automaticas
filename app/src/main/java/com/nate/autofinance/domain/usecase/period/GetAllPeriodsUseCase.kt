package com.nate.autofinance.domain.usecase.period

import android.content.Context
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.domain.models.FinancialPeriod
import com.nate.autofinance.utils.SessionManager

class GetAllPeriodsForUserUseCase(
    private val periodRepository: PeriodRepository,
    private val session: SessionManager,
    private val context: Context
) {
    suspend operator fun invoke(): List<FinancialPeriod> {
        val userId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")
        return periodRepository.getPeriodsForUser(userId)
    }
}

