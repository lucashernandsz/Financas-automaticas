package com.nate.autofinance.data.repository

import com.nate.autofinance.data.local.FinancialPeriodDao
import com.nate.autofinance.data.remote.FirebasePeriodService
import com.nate.autofinance.domain.models.FinancialPeriod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PeriodRepository(
    private val periodDao: FinancialPeriodDao,
    private val periodService: FirebasePeriodService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val TAG = "FinancialPeriodRepo"
    }

    suspend fun insert(period: FinancialPeriod) = withContext(ioDispatcher) {
        periodDao.insert(period)
    }

    suspend fun insertAll(periods: List<FinancialPeriod>) = withContext(ioDispatcher) {
        periodDao.insertAll(periods)
    }

    suspend fun update(period: FinancialPeriod) = withContext(ioDispatcher) {
        periodDao.update(period)
    }

    suspend fun delete(period: FinancialPeriod) = withContext(ioDispatcher) {
        periodDao.delete(period)
    }

    suspend fun selectOnly(periodId: Int) = withContext(ioDispatcher) {
        periodDao.selectOnly(periodId)
    }

    suspend fun getFinancialPeriodsForUser(userId: Int?): List<FinancialPeriod> = withContext(ioDispatcher) {
        periodDao.getPeriodsByUserId(userId)
    }

    suspend fun getSelectedPeriodForUser(userId: Int): FinancialPeriod? = withContext(ioDispatcher) {
        periodDao.getSelectedPeriodByUserId(userId)
    }

    suspend fun getPeriodForUser(userId: Int, periodId: Int): FinancialPeriod? = withContext(ioDispatcher) {
        periodDao.getPeriodByUserIdAndPeriodId(userId, periodId)
    }

    suspend fun getPeriodsForUser(userId: Int): List<FinancialPeriod> = withContext(ioDispatcher) {
        periodDao.getPeriodsByUserId(userId)
    }

}
