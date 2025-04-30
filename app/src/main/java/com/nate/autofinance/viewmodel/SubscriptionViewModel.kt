package com.nate.autofinance.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.domain.usecases.subscription.ToggleSubscriptionUseCase
import com.nate.autofinance.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val toggleUseCase: ToggleSubscriptionUseCase =
        ServiceLocator.toggleSubscriptionUseCase
    // Estado de assinatura
    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed

    // Estado de leitura de notificações (se já está habilitado nas Configurações)
    private val _isReadingEnabled = MutableStateFlow(
        NotificationManagerCompat.getEnabledListenerPackages(application)
            .contains(application.packageName)
    )
    val isReadingEnabled: StateFlow<Boolean> = _isReadingEnabled

    init {
        // Carrega o estado inicial de assinatura do usuário
        viewModelScope.launch {
            val uid = SessionManager.getUserId(appContext) ?: return@launch
            val user = ServiceLocator.userRepository.getUserById(uid) ?: return@launch
            _isSubscribed.value = user.isSubscribed
        }
    }

    /** Ativa Premium (sem tocar na leitura de notificações) */
    fun activatePremium(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            toggleUseCase(true)
            _isSubscribed.value = true
            onComplete()
        }
    }

    /** Abre as Configurações para permitir leitura de notificações */
    fun enableNotificationReading() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }

    /** Atualiza o estado de leitura após o usuário voltar das Configurações */
    fun refreshReadingStatus() {
        _isReadingEnabled.value = NotificationManagerCompat.getEnabledListenerPackages(appContext)
            .contains(appContext.packageName)
    }
}
