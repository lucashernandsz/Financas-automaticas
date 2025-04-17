package com.nate.autofinance.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nate.autofinance.domain.models.FinancialPeriod

@Dao
interface FinancialPeriodDao {
    @Insert
    suspend fun insert(period: FinancialPeriod): Long

    @Update
    suspend fun update(period: FinancialPeriod): Int

    @Delete
    suspend fun delete(period: FinancialPeriod): Int

    @Query("SELECT * FROM financial_period WHERE id = :id")
    suspend fun getPeriodById(id: Int): FinancialPeriod?

    @Query("SELECT * FROM financial_period WHERE userId = :userId")
    suspend fun getPeriodsByUserId(userId: Long?): List<FinancialPeriod>

    @Query("SELECT * FROM financial_period WHERE userId = :userId AND isSelected = 1")
    suspend fun getSelectedPeriodByUserId(userId: Long): FinancialPeriod?

    @Query("SELECT * FROM financial_period WHERE userId = :userId AND id = :periodId")
    suspend fun getPeriodByUserIdAndPeriodId(userId: Long, periodId: Int) : FinancialPeriod?

}
