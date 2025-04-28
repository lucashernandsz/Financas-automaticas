package com.nate.autofinance.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.domain.usecases.period.CreateNewPeriodUseCase
import com.nate.autofinance.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para iniciar novo período financeiro.
 * Agora não recebe userId; use case usa SessionManager internamente.
 */
class NewPeriodViewModel : ViewModel() {
    private val createNewPeriodUseCase = ServiceLocator.createNewPeriodUseCase

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun startNewPeriod() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                createNewPeriodUseCase()
                _success.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Erro ao iniciar novo período"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
    fun clearSuccess() { _success.value = false }
}
