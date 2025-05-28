package com.nate.autofinance.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nate.autofinance.domain.models.SyncStatus
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction): Int

    @Delete
    suspend fun delete(transaction: Transaction): Int

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    @Query("SELECT * FROM `transaction` WHERE financialPeriodId = :financialPeriodId")
    suspend fun getTransactionsByFinancialPeriodId(financialPeriodId: Int): List<Transaction>

    @Query("SELECT * FROM `transaction` WHERE syncStatus != :synced")
    suspend fun getPendingTransactions(synced: SyncStatus = SyncStatus.SYNCED): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Query("""
      SELECT * 
      FROM `transaction`
      WHERE financialPeriodId = :financialPeriodId
      ORDER BY date DESC
    """)
    fun observeTransactionsByPeriodId(financialPeriodId: Int): Flow<List<Transaction>>
}


