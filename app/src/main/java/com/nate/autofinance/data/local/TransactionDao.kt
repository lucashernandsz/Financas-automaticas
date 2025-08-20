package com.nate.autofinance.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // ✅ REATIVO: Esta query emite automaticamente quando há mudanças na tabela
    @Query("SELECT * FROM `transaction` WHERE financialPeriodId = :periodId ORDER BY date DESC")
    fun observeTransactionsByPeriodId(periodId: Int): Flow<List<Transaction>>

    // ✅ REATIVO: Observa transações pendentes de sincronização
    @Query("SELECT * FROM `transaction` WHERE syncStatus = 'PENDING'")
    fun observePendingTransactions(): Flow<List<Transaction>>

    // ✅ REATIVO: Observa todas as transações de um usuário
    @Query("SELECT * FROM `transaction` WHERE userId = :userId ORDER BY date DESC")
    fun observeTransactionsByUserId(userId: Int): Flow<List<Transaction>>

    // Queries tradicionais (não reativas) para casos específicos
    @Query("SELECT * FROM `transaction` WHERE financialPeriodId = :periodId ORDER BY date DESC")
    suspend fun getTransactionsByFinancialPeriodId(periodId: Int): List<Transaction>

    @Query("SELECT * FROM `transaction` WHERE syncStatus = 'PENDING'")
    suspend fun getPendingTransactions(): List<Transaction>

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    @Query("SELECT * FROM `transaction` WHERE userId = :userId ORDER BY date DESC")
    suspend fun getTransactionsByUserId(userId: Int): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    // Queries auxiliares para estatísticas reativas
    @Query("SELECT COUNT(*) FROM `transaction` WHERE financialPeriodId = :periodId")
    fun observeTransactionCount(periodId: Int): Flow<Int>

    @Query("SELECT SUM(amount) FROM `transaction` WHERE financialPeriodId = :periodId AND amount > 0")
    fun observeTotalIncome(periodId: Int): Flow<Double?>

    @Query("SELECT SUM(amount) FROM `transaction` WHERE financialPeriodId = :periodId AND amount < 0")
    fun observeTotalExpenses(periodId: Int): Flow<Double?>

    @Query("SELECT * FROM `transaction` WHERE firebaseDocId = :docId LIMIT 1")
    suspend fun getTransactionByFirebaseDocId(docId: String): Transaction?
}