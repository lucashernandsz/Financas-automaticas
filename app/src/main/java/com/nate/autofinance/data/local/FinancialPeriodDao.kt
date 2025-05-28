package com.nate.autofinance.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nate.autofinance.domain.models.FinancialPeriod
import com.nate.autofinance.domain.models.SyncStatus

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
    suspend fun getPeriodsByUserId(userId: Int?): List<FinancialPeriod>

    @Query("SELECT * FROM financial_period WHERE userId = :userId AND isSelected = 1")
    suspend fun getSelectedPeriodByUserId(userId: Int): FinancialPeriod?

    @Query("SELECT * FROM financial_period WHERE userId = :userId AND id = :periodId")
    suspend fun getPeriodByUserIdAndPeriodId(userId: Int, periodId: Int) : FinancialPeriod?

    @Query("SELECT * FROM financial_period WHERE syncStatus != :synced")
    suspend fun getPendingPeriods(synced: SyncStatus = SyncStatus.SYNCED): List<FinancialPeriod>

    @Query("SELECT * FROM financial_period WHERE firebaseDocId = :docId")
    suspend fun getPeriodByFirebaseDocId(docId: String): FinancialPeriod?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(periods: List<FinancialPeriod>)


}
