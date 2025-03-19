package com.nate.autofinance.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nate.autofinance.domain.models.Transaction

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
}
