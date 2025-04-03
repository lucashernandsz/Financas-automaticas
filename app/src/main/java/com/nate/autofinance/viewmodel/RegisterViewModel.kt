package com.nate.autofinance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.nate.autofinance.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Define os possíveis estados do cadastro
sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: FirebaseUser) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    // Estado interno que a ViewModel gerencia
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    // Estado exposto para a UI (imutável)
    val registerState: StateFlow<RegisterState> = _registerState

    /**
     * Função para realizar o cadastro.
     * Recebe o nome, email, senha e confirmação da senha.
     * Faz validações básicas e chama o método de registro do AuthRepository.
     */
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        // Valida os campos antes de prosseguir
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _registerState.value = RegisterState.Error("Preencha todos os campos!")
            return
        }
        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("As senhas não coincidem!")
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val user = authRepository.registerUser(email, password)
                if (user != null) {
                    // Aqui, se necessário, você pode atualizar o displayName do usuário com o nome informado
                    _registerState.value = RegisterState.Success(user)
                } else {
                    _registerState.value = RegisterState.Error("Erro ao registrar usuário.")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Erro desconhecido.")
            }
        }
    }

    // Função opcional para resetar o estado para Idle
    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}
