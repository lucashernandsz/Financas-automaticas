package com.nate.autofinance.domain.usecases

import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.data.local.TransactionDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

// Fake para simular o comportamento do TransactionDao
class FakeTransactionDao : TransactionDao {
    val insertedTransactions = mutableListOf<Transaction>()

    override suspend fun insert(transaction: Transaction): Long {
        insertedTransactions.add(transaction)
        return insertedTransactions.size.toLong() // Simula o ID gerado
    }

    override suspend fun update(transaction: Transaction): Int { TODO("Not yet implemented") }

    override suspend fun delete(transaction: Transaction): Int { TODO("Not yet implemented") }
    override suspend fun getTransactionById(id: Int): Transaction? {
        TODO("Not yet implemented")
    }

    override suspend fun getTransactionByFinancialPeriodId(periodId: Int): List<Transaction> {
        return emptyList()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionUseCaseTest {

    @Test
    fun `invoke should add transaction using repository`() = runTest {
        // Arrange: cria o FakeTransactionDao e injeta no TransactionRepository
        val fakeDao = FakeTransactionDao()
        val repository = TransactionRepository(fakeDao)
        val addTransactionUseCase = AddTransactionUseCase(repository)
        val testTransaction = Transaction(
            id = 0,
            date = Date(),
            amount = 50.0,
            description = "Unit Test Transaction",
            category = "Ganho",
            userId = "user123",
            imported = false
        )

        // Act: chama o use case para adicionar a transação
        addTransactionUseCase(testTransaction)

        // Assert: verifica se a transação foi inserida no fake DAO
        assertEquals(1, fakeDao.insertedTransactions.size)
        assertEquals(testTransaction, fakeDao.insertedTransactions.first())
    }
}
