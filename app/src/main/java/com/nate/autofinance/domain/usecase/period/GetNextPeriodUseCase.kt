package com.nate.autofinance.domain.usecase.period

class GetNextPeriodUseCase(
    private val periodRepository: com.nate.autofinance.data.repository.PeriodRepository,
    private val getCurrentActivePeriodUseCase: GetCurrentActivePeriodUseCase
) {
    suspend operator fun invoke(): com.nate.autofinance.domain.models.FinancialPeriod {
        val currentPeriod = getCurrentActivePeriodUseCase()
        val userId = currentPeriod.userId
            ?: throw IllegalStateException("Usuário não autenticado ou período atual inválido.")

        val nextPeriod = periodRepository.getNextPeriodForUser(userId)
        return nextPeriod ?: throw IllegalStateException("Próximo período não encontrado.")
    }
}