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
        periodRepository.selectOnly(periodId)
        session.saveSelectedPeriodId(context, periodId)
    }
}