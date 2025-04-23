// CreateDefaultPeriodUseCase.kt
package com.nate.autofinance.domain.usecases.period

import android.os.Build
import androidx.annotation.RequiresApi
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.domain.models.FinancialPeriod
import com.nate.autofinance.utils.toDate
import java.time.LocalDate

/**
 * Cria o período padrão (mês corrente), mas só se o usuário ainda não tiver nenhum.
 * Marca este período como selecionado.
 */
class CreateDefaultPeriodUseCase(
    private val periodRepository: PeriodRepository
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(userId: Long?) {
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

        // 3) Persiste local + remoto via repository
        periodRepository.addFinancialPeriod(defaultPeriod)
    }
}
