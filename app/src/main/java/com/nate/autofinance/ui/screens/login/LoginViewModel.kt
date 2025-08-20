// app/src/main/java/com/nate/autofinance/viewmodel/LoginViewModel.kt
package com.nate.autofinance.ui.screens.login

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseUser
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.data.auth.AuthRepository
import com.nate.autofinance.data.sync.SyncWorker
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
    private val appContext = getApplication<Application>()

    @RequiresApi(Build.VERSION_CODES.O)
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                // 1. Autentica no FirebaseAuth
                val firebaseUser = authRepository.loginUser(email, password)
                    ?: throw IllegalStateException("Falha na autenticação")

                // 2. Cria ou obtém o usuário local, já com firebaseDocId
                val localUser = userRepository.getOrCreateUser(firebaseUser)
                SessionManager.saveUserId(appContext, localUser.id)

                // 3. Executa sincronização imediata ANTES de navegar
                val syncManager = ServiceLocator.syncManager
                val createDefaultPeriod = ServiceLocator.createDefaultPeriodUseCase

                try {
                    // Sincroniza dados remotos
                    syncManager.syncAll()

                    // Garante que existe um período selecionado
                    val currentPeriod = SessionManager.getSelectedPeriodId(appContext)
                    if (currentPeriod == null) {
                        createDefaultPeriod()
                    }
                } catch (syncError: Exception) {
                    // Log do erro mas não falha o login
                    println("LoginViewModel: Erro na sincronização inicial: ${syncError.message}")
                }

                _loginState.value = LoginState.Success(firebaseUser)

                // Agenda sync periódico para depois
                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()

                WorkManager
                    .getInstance(appContext)
                    .enqueueUniqueWork(
                        "periodic_sync",
                        ExistingWorkPolicy.REPLACE,
                        syncRequest
                    )
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
