package com.nate.autofinance.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nate.autofinance.data.local.Converters
import com.nate.autofinance.data.local.FinancialPeriodDao
import com.nate.autofinance.data.local.TransactionDao
import com.nate.autofinance.data.local.UserDao
import com.nate.autofinance.domain.models.User
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.domain.models.FinancialPeriod

@Database(
    entities = [User::class, Transaction::class, FinancialPeriod::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun financialPeriodDao(): FinancialPeriodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auto_finance_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
