// app/src/main/java/com/nate/autofinance/domain/usecases/CreateNewPeriodUseCase.kt
package com.nate.autofinance.domain.usecases.period

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.domain.models.FinancialPeriod
import com.nate.autofinance.utils.SessionManager
import com.nate.autofinance.utils.toDate
import java.time.LocalDate

class CreateNewPeriodUseCase(
    private val periodRepository: PeriodRepository,
    private val session: SessionManager,
    private val context: Context
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke() {
        val userId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")

        val today = LocalDate.now().toDate()

        val old = periodRepository.getSelectedPeriodForUser(userId)
        if (old != null) {
            val closedOld = old.copy(
                endDate = today,
                isSelected = false
            )
            periodRepository.updateFinancialPeriod(closedOld)
        }

        val newPeriod = FinancialPeriod(
            startDate = today,
            endDate = null,
            isSelected = true,
            userId = userId
        )
        periodRepository.addFinancialPeriod(newPeriod)
    }
}
