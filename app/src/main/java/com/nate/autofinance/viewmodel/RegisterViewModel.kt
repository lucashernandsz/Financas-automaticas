// app/src/main/java/com/nate/autofinance/viewmodel/RegisterViewModel.kt
package com.nate.autofinance.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.nate.autofinance.data.auth.AuthRepository
import com.nate.autofinance.data.repository.UserRepository
import com.nate.autofinance.domain.models.User
import com.nate.autofinance.domain.usecases.period.CreateDefaultPeriodUseCase
import com.nate.autofinance.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle    : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: FirebaseUser) : RegisterState()
    data class Error  (val message: String)   : RegisterState()
}

class RegisterViewModel(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository,
    private val createDefaultPeriod: CreateDefaultPeriodUseCase
) : AndroidViewModel(application) {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val appContext = application

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
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
                val firebaseUser = authRepository.registerUser(email, password)
                    ?: throw IllegalStateException("Falha no Auth")

                val domainUser = User(
                    name         = name,
                    email        = firebaseUser.email.orEmpty(),
                    isSubscribed = false
                )
                val localUserId = userRepository.addUser(domainUser)

                SessionManager.saveUserId(appContext, localUserId)

                createDefaultPeriod(localUserId)

                _registerState.value = RegisterState.Success(firebaseUser)

            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Erro desconhecido.")
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}
