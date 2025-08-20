// CreateDefaultPeriodUseCase.kt
package com.nate.autofinance.domain.usecases.period

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.domain.models.FinancialPeriod
import com.nate.autofinance.utils.SessionManager
import com.nate.autofinance.utils.toDate
import java.time.LocalDate

/**
 * Cria o período padrão (mês corrente), mas só se o usuário ainda não tiver nenhum.
 * Marca este período como selecionado.
 */
class CreateDefaultPeriodUseCase(
    private val periodRepository: PeriodRepository,
    private val session: SessionManager,
    private val context: Context
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke() {
        val userId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")

        val existing = periodRepository.getFinancialPeriodsForUser(userId)
        if (existing.isNotEmpty()) return

        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)

        val defaultPeriod = FinancialPeriod(
            startDate = startOfMonth.toDate(),
            endDate = null,
            isSelected = true,
            userId = userId
        )

        periodRepository.insert(defaultPeriod)

        // Persiste o período criado como selecionado na sessão
        val created = periodRepository.getSelectedPeriodForUser(userId)
        created?.let { session.saveSelectedPeriodId(context, it.id) }
    }
}
