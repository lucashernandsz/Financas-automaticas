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
class LoginViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginViewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Configura o dispatcher principal para testes
        Dispatchers.setMain(testDispatcher)

        // Cria um mock do AuthRepository
        authRepository = mock()
        // Injeta o mock na ViewModel
        loginViewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        // Restaura o dispatcher principal para o original
        Dispatchers.resetMain()
    }

    @Test
    fun `login success updates state to Success`() = runTest {
        val fakeUser: FirebaseUser = mock()
        // Configura o mock para retornar fakeUser
        whenever(authRepository.loginUser("test@example.com", "password"))
            .thenReturn(fakeUser)

        // Chama a função de login na ViewModel
        loginViewModel.login("test@example.com", "password")

        // Aguarda o término das coroutines
        advanceUntilIdle()

        // Verifica se o estado atual é Success com o fakeUser
        val state = loginViewModel.loginState.value
        assertEquals(LoginState.Success(fakeUser), state)
    }

    @Test
    fun `login returns null updates state to Error`() = runTest {
        // Configura o mock para retornar null
        whenever(authRepository.loginUser("test@example.com", "password"))
            .thenReturn(null)

        loginViewModel.login("test@example.com", "password")
        advanceUntilIdle()

        // Verifica se o estado é Error com a mensagem esperada
        val state = loginViewModel.loginState.value
        assertEquals(
            LoginState.Error("Usuário não encontrado ou credenciais inválidas"),
            state
        )
    }

    @Test
    fun `login exception updates state to Error`() = runTest {
        // Configura o mock para lançar uma exceção não verificada (RuntimeException)
        val exception = RuntimeException("Network error")
        whenever(authRepository.loginUser("test@example.com", "password"))
            .thenThrow(exception)

        loginViewModel.login("test@example.com", "password")
        advanceUntilIdle()

        // Verifica se o estado é Error com a mensagem da exceção
        val state = loginViewModel.loginState.value
        assertEquals(LoginState.Error("Network error"), state)
    }
}
