package com.nate.autofinance.ui.screens.settings.subscription

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.data.repository.SettingsRepository
import com.nate.autofinance.domain.usecases.subscription.ToggleSubscriptionUseCase
import com.nate.autofinance.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    // pego o contexto de aplicação apenas uma vez
    private val appContext = application.applicationContext

    private val toggleUseCase: ToggleSubscriptionUseCase =
        ServiceLocator.toggleSubscriptionUseCase

    // Use appContext aqui em vez de "applicationContext"
    private val settingsRepo = SettingsRepository(appContext)

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    // estado da preferência de importação
    val isImportEnabled: StateFlow<Boolean> = settingsRepo.notificationImportEnabled
        .stateIn(viewModelScope, SharingStarted.Companion.Eagerly, false)

    private val _hasPermission = MutableStateFlow(
        NotificationManagerCompat.getEnabledListenerPackages(appContext)
            .contains(appContext.packageName)
    )
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    init {
        // carrega assinatura do usuário remoto
        viewModelScope.launch {
            val uid = SessionManager.getUserId(appContext) ?: return@launch
            val user = ServiceLocator.userRepository.getUserById(uid) ?: return@launch
            _isSubscribed.value = user.isSubscribed
        }
    }

    fun activatePremium(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            toggleUseCase(true)
            _isSubscribed.value = true
            onComplete()
        }
    }

    fun toggleImportEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setNotificationImportEnabled(enabled)
        }
    }

    fun enableNotificationReading() {
        // abre tela de acesso a notificações do sistema
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }

    fun refreshPermissionState() {
        _hasPermission.value = NotificationManagerCompat
            .getEnabledListenerPackages(appContext)
            .contains(appContext.packageName)
    }
}