package com.nate.autofinance.viewmodel

import com.google.firebase.auth.FirebaseUser
import com.nate.autofinance.data.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var registerViewModel: RegisterViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Configura o dispatcher principal para testes
        Dispatchers.setMain(testDispatcher)
        // Cria um mock do AuthRepository
        authRepository = mock()
        // Injeta o mock na ViewModel
        registerViewModel = RegisterViewModel(authRepository)
    }

    @After
    fun tearDown() {
        // Restaura o dispatcher principal para o original
        Dispatchers.resetMain()
    }

    @Test
    fun `register with empty fields updates state to Error`() = runTest {
        // Chama o método register com algum campo vazio
        registerViewModel.register("", "test@example.com", "password", "password")
        // Como a validação é feita antes de iniciar a coroutine, não é necessário esperar
        assertEquals(RegisterState.Error("Preencha todos os campos!"), registerViewModel.registerState.value)
    }

    @Test
    fun `register with mismatched passwords updates state to Error`() = runTest {
        // Chama o método register com senhas diferentes
        registerViewModel.register("Test", "test@example.com", "password", "different")
        assertEquals(RegisterState.Error("As senhas não coincidem!"), registerViewModel.registerState.value)
    }

    @Test
    fun `successful registration updates state to Success`() = runTest {
        val fakeUser: FirebaseUser = mock()
        // Configura o mock para retornar o usuário fake ao registrar
        whenever(authRepository.registerUser("test@example.com", "password"))
            .thenReturn(fakeUser)

        registerViewModel.register("Test", "test@example.com", "password", "password")
        // Aguarda a execução da coroutine
        advanceUntilIdle()

        assertEquals(RegisterState.Success(fakeUser), registerViewModel.registerState.value)
    }

    @Test
    fun `registration returns null updates state to Error`() = runTest {
        // Configura o mock para retornar null, simulando uma falha no registro
        whenever(authRepository.registerUser("test@example.com", "password"))
            .thenReturn(null)

        registerViewModel.register("Test", "test@example.com", "password", "password")
        advanceUntilIdle()

        assertEquals(RegisterState.Error("Erro ao registrar usuário."), registerViewModel.registerState.value)
    }

    @Test
    fun `registration exception updates state to Error`() = runTest {
        // Configura o mock para lançar uma exceção não verificada
        val exception = RuntimeException("Network error")
        whenever(authRepository.registerUser("test@example.com", "password"))
            .thenThrow(exception)

        registerViewModel.register("Test", "test@example.com", "password", "password")
        advanceUntilIdle()

        assertEquals(RegisterState.Error("Network error"), registerViewModel.registerState.value)
    }
}
