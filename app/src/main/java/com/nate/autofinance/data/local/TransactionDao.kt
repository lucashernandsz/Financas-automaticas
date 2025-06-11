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

    // ✅ REATIVO: Esta query emite automaticamente quando há mudanças na tabela
    @Query("SELECT * FROM `transaction` WHERE financialPeriodId = :periodId ORDER BY date DESC")
    fun observeTransactionsByPeriod(periodId: Int): Flow<List<Transaction>>

    // ✅ REATIVO: Observa transações pendentes de sincronização
    @Query("SELECT * FROM `transaction` WHERE syncStatus = 'PENDING'")
    fun observePendingTransactions(): Flow<List<Transaction>>

    // Queries tradicionais (não reativas) para casos específicos
    @Query("SELECT * FROM `transaction` WHERE financialPeriodId = :periodId ORDER BY date DESC")
    suspend fun getTransactionsByPeriod(periodId: Int): List<Transaction>

    @Query("SELECT * FROM `transaction` WHERE syncStatus = 'PENDING'")
    suspend fun getPendingTransactions(): List<Transaction>

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
}


