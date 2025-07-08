// app/src/main/java/com/nate/autofinance/domain/usecases/period/SelectPeriodUseCase.kt

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

        // Busca todos os períodos do usuário
        val allPeriods = periodRepository.getPeriodsForUser(userId)

        // Itera sobre todos os períodos e atualiza cada um APENAS UMA VEZ
        allPeriods.forEach { period ->
            // Define 'isSelected' como true apenas se o ID corresponder ao período clicado
            val shouldBeSelected = period.id == periodId

            // Se o estado atual for diferente do que deveria ser, atualiza
            if (period.isSelected != shouldBeSelected) {
                val updatedPeriod = period.copy(isSelected = shouldBeSelected)
                periodRepository.updateFinancialPeriod(updatedPeriod)
            }
        }

        // Salva o ID do período selecionado na sessão para notificar outras partes do app
        session.saveSelectedPeriodId(context, periodId)
    }
}