package com.nate.autofinance.ui.screens.register

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.nate.autofinance.data.auth.AuthRepository
import com.nate.autofinance.domain.models.User
import com.nate.autofinance.utils.SessionManager
import com.nate.autofinance.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle    : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: FirebaseUser) : RegisterState()
    data class Error(val message: String)   : RegisterState()
}

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val userRepository = ServiceLocator.userRepository
    private val createDefaultPeriodUseCase = ServiceLocator.createDefaultPeriodUseCase
    private val appContext = application

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _state

    @RequiresApi(Build.VERSION_CODES.O)
    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _state.value = RegisterState.Error("Preencha todos os campos!")
            return
        }
        if (password != confirmPassword) {
            _state.value = RegisterState.Error("Senhas não coincidem!")
            return
        }

        viewModelScope.launch {
            _state.value = RegisterState.Loading
            try {
                val firebaseUser = authRepository.registerUser(email, password)
                    ?: throw IllegalStateException("Falha no Auth")

                val domainUser = User(name = name, email = email, isSubscribed = false)
                val localUserId = userRepository.addUser(domainUser)
                    ?: throw IllegalStateException("Falha ao criar usuário local")

                SessionManager.saveUserId(appContext, localUserId.toInt())
                createDefaultPeriodUseCase()

                _state.value = RegisterState.Success(firebaseUser)
            } catch (e: Exception) {
                _state.value = RegisterState.Error(e.message ?: "Erro desconhecido.")
            }
        }
    }

    fun resetState() { _state.value = RegisterState.Idle }
}
