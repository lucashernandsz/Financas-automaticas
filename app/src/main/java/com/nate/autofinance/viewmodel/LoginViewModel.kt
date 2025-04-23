// app/src/main/java/com/nate/autofinance/viewmodel/LoginViewModel.kt
package com.nate.autofinance.viewmodel

import SyncWorker
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseUser
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.data.auth.AuthRepository
import com.nate.autofinance.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle    : LoginState()
    object Loading : LoginState()
    data class Success(val user: FirebaseUser) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    // instanciamos o repositório aqui, não no construtor
    private val authRepository = AuthRepository()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val userRepository      = ServiceLocator.userRepository
    private val createDefaultPeriod = ServiceLocator.createDefaultPeriodUseCase
    private val appContext = getApplication<Application>()

    @RequiresApi(Build.VERSION_CODES.O)
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val firebaseUser = authRepository.loginUser(email, password)
                    ?: throw IllegalStateException("Falha na autenticação")
                val localUser = userRepository.getOrCreateUser(firebaseUser)
                SessionManager.saveUserId(appContext, localUser.id.toInt())

                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .build()
                WorkManager
                    .getInstance(appContext)
                    .enqueue(syncRequest)

                _loginState.value = LoginState.Success(firebaseUser)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()

            WorkManager
                .getInstance(appContext)
                .cancelUniqueWork("auto_sync")

            SessionManager.clear(getApplication())

            _loginState.value = LoginState.Idle
        }
    }
}
