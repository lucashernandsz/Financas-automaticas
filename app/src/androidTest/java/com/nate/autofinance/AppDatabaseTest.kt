package com.nate.autofinance

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nate.autofinance.data.local.AppDatabase
import com.nate.autofinance.data.local.FinancialPeriodDao
import com.nate.autofinance.data.local.TransactionDao
import com.nate.autofinance.data.local.UserDao
import com.nate.autofinance.domain.models.FinancialPeriod
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.domain.models.User
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var financialPeriodDao: FinancialPeriodDao

    private lateinit var user: User
    private lateinit var transaction: Transaction
    private lateinit var financialPeriod: FinancialPeriod

    @Before
    fun createDb() {
        // Create an in-memory database for testing.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries() // Only for testing purposes.
            .build()
        userDao = database.userDao()
        transactionDao = database.transactionDao()
        financialPeriodDao = database.financialPeriodDao()

    }

    @Before
    fun createEntities() {
        user = User(
            name = "John Doe",
            email = "john@example.com",
        )

        transaction = Transaction(
            amount = 100.0,
            description = "Test transaction",
            date = Date(),
            category = "Income",
            id = TODO(),
            userId = TODO(),
            financialPeriodId = TODO(),
            imported = TODO(),
            syncStatus = TODO(),
            firebaseDocFinancialPeriodId = TODO(),
            firebaseDocUserId = TODO(),
            firebaseDocId = TODO()
        )

        financialPeriod = FinancialPeriod(
            startDate = Date(),
            endDate = Date(),
            totalIncome = 100.0,
            totalExpenses = 0.0,
            userId = 0,
        )
    }

    @After
    fun closeDb() {
        // Clean up and close the database.
        database.close()
    }

    @Test
    fun insertAndRetrieveUser() = runBlocking {
        // Insert the user into the database.
        userDao.insert(user)

        // Retrieve the user. Assuming the first inserted user has an ID of 1.
        val retrievedUser = userDao.getUserById(1)

        // Validate that the retrieved user has the expected name.
        assertThat(retrievedUser?.name, equalTo("John Doe"))
    }

    @Test
    fun updateUser() = runBlocking {
        // Insert the user into the database.
        userDao.insert(user)

        // Retrieve the user. Assuming the first inserted user has an ID of 1.
        val retrievedUser = userDao.getUserById(1)

        var updatedUser = retrievedUser?.copy(
            name = "Jane Doe"
        )

        userDao.update(updatedUser!!)

        val retrievedUpdatedUser = userDao.getUserById(1)

        assertThat(retrievedUpdatedUser?.name, equalTo("Jane Doe"))
    }

    @Test
    fun deleteUser() = runBlocking {
        // Insert the user into the database.
        userDao.insert(user)

        // Retrieve the user. Assuming the first inserted user has an ID of 1.
        val retrievedUser = userDao.getUserById(1)

        userDao.delete(retrievedUser!!)

        val retrievedDeletedUser = userDao.getUserById(1)

        assertThat(retrievedDeletedUser, equalTo(null))
    }

    @Test
    fun insertAndRetrieveTransaction() = runBlocking {
        // Insert the transaction into the database.
        database.transactionDao().insert(transaction)

        // Retrieve the transaction. Assuming the first inserted transaction has an ID of 1.
        val retrievedTransaction = database.transactionDao().getTransactionById(1)

        // Validate that the retrieved transaction has the expected amount.
        assertThat(retrievedTransaction?.amount, equalTo(100.0))
    }

    @Test
    fun updateTransaction() = runBlocking {
        // Insert the transaction into the database.
        database.transactionDao().insert(transaction)

        // Retrieve the transaction. Assuming the first inserted transaction has an ID of 1.
        val retrievedTransaction = transactionDao.getTransactionById(1)

        var updatedTransaction = retrievedTransaction?.copy(
            amount = 200.0
        )

        database.transactionDao().update(updatedTransaction!!)

        val retrievedUpdatedTransaction = database.transactionDao().getTransactionById(1)

        assertThat(retrievedUpdatedTransaction?.amount, equalTo(200.0))
    }

    @Test
    fun deleteTransaction() = runBlocking {
        // Insert the transaction into the database.
        database.transactionDao().insert(transaction)

        // Retrieve the transaction. Assuming the first inserted transaction has an ID of 1.
        val retrievedTransaction = transactionDao.getTransactionById(1)

        database.transactionDao().delete(retrievedTransaction!!)

        val retrievedDeletedTransaction = database.transactionDao().getTransactionById(1)

        assertThat(retrievedDeletedTransaction, equalTo(null))
    }

    @Test
    fun insertAndRetrieveFinancialPeriod() = runBlocking {
        // Insert the financial period into the database.
        financialPeriodDao.insert(financialPeriod)

        // Retrieve the financial period. Assuming the first inserted financial period has an ID of 1.
        val retrievedPeriod = financialPeriodDao.getPeriodById(1)

        // Validate that the retrieved financial period has the expected initial balance.
        assertThat(retrievedPeriod?.totalIncome, equalTo(100.0))
    }

    @Test
    fun updateFinancialPeriod() = runBlocking {
        // Insert the financial period into the database.
        financialPeriodDao.insert(financialPeriod)

        // Retrieve the financial period. Assuming the first inserted financial period has an ID of 1.
        val retrievedPeriod = financialPeriodDao.getPeriodById(1)

        val updatedPeriod = retrievedPeriod?.copy(
            totalIncome = 2000.0
        )

        financialPeriodDao.update(updatedPeriod!!)

        val retrievedUpdatedPeriod = financialPeriodDao.getPeriodById(1)

        assertThat(retrievedUpdatedPeriod?.totalIncome, equalTo(2000.0))
    }

    @Test
    fun deleteFinancialPeriod() = runBlocking {
        // Insert the financial period into the database.
        financialPeriodDao.insert(financialPeriod)

        // Retrieve the financial period. Assuming the first inserted financial period has an ID of 1.
        val retrievedPeriod = financialPeriodDao.getPeriodById(1)

        financialPeriodDao.delete(retrievedPeriod!!)

        val retrievedDeletedPeriod = financialPeriodDao.getPeriodById(1)

        assertThat(retrievedDeletedPeriod, equalTo(null))
    }
}
