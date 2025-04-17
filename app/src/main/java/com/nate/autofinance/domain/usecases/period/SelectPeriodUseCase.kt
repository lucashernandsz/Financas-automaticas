import com.nate.autofinance.data.repository.PeriodRepository

class SelectPeriodUseCase(
    private val periodRepo: PeriodRepository
) {
    suspend operator fun invoke(periodId: Int, userId: Long) {
        val all = periodRepo.getPeriodsForUser(userId)
        all.forEach {
            val updated = it.copy(isSelected = (it.id == periodId))
            periodRepo.updateFinancialPeriod(updated)
        }
    }
}
