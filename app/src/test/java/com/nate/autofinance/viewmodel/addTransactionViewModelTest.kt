package com.nate.autofinance.viewmodel

import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.domain.usecases.transaction.AddTransactionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    // Cria um dispatcher de teste para substituir o Main dispatcher.
    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    @Before
    fun setUp() {
        // Configura o Main dispatcher para o dispatcher de teste.
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // Reseta o Main dispatcher para o padrão após os testes.
        Dispatchers.resetMain()
    }

    // Transação fake para os testes.
    private val fakeTransaction = Transaction(
        id = 0,
        date = Date(),
        amount = 100.0,
        description = "Transação de Teste",
        category = "Ganho",
        userId = 0,
        financialPeriodId = 0,
        imported = false,
        firebaseDocId = "transaction"
    )

    @Test
    fun `when addTransaction is successful then state emits Success`() = runTest {
        // Arrange: Cria um mock do AddTransactionUseCase e configura o stubbing para sucesso.
        val addTransactionUseCase = mock(AddTransactionUseCase::class.java)
        doReturn(Unit).`when`(addTransactionUseCase).invoke(fakeTransaction)

        val viewModel = AddTransactionViewModel(addTransactionUseCase)

        // Act: Chama a função addTransaction e aguarda a execução das coroutines.
        viewModel.addTransaction(fakeTransaction)
        advanceUntilIdle()

        // Assert: Verifica que o estado final emitido pelo ViewModel é Success.
        assertEquals(AddTransactionState.Success, viewModel.state.value)
    }

    @Test
    fun `when addTransaction fails then state emits Error`() = runTest {
        // Arrange: Configura o mock para lançar uma RuntimeException com uma mensagem de erro.
        val errorMessage = "Erro ao adicionar transação"
        val addTransactionUseCase = mock(AddTransactionUseCase::class.java)
        doThrow(RuntimeException(errorMessage)).`when`(addTransactionUseCase).invoke(fakeTransaction)

        val viewModel = AddTransactionViewModel(addTransactionUseCase)

        // Act: Chama a função addTransaction e aguarda a conclusão das coroutines.
        viewModel.addTransaction(fakeTransaction)
        advanceUntilIdle()

        // Assert: Verifica se o estado emitido é Error com a mensagem correta.
        when (val state = viewModel.state.value) {
            is AddTransactionState.Error -> assertEquals(errorMessage, state.message)
            else -> fail("Estado esperado: Error, mas foi: $state")
        }
    }
}
