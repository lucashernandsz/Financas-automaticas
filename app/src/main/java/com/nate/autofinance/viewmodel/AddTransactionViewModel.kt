// app/src/main/java/com/nate/autofinance/viewmodel/AddTransactionViewModel.kt
package com.nate.autofinance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AddTransactionState {
    object Idle    : AddTransactionState()
    object Loading : AddTransactionState()
    object Success : AddTransactionState()
    data class Error(val message: String) : AddTransactionState()
}

class AddTransactionViewModel : ViewModel() {

    private val addTransactionUseCase = ServiceLocator.addTransactionUseCase

    private val _state = MutableStateFlow<AddTransactionState>(AddTransactionState.Idle)
    val state: StateFlow<AddTransactionState> = _state

    /** Insere a transação e atualiza o stateFlow */
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _state.value = AddTransactionState.Loading
            try {
                addTransactionUseCase(transaction)
                _state.value = AddTransactionState.Success
            } catch (e: Exception) {
                _state.value = AddTransactionState.Error(e.message ?: "Erro ao adicionar")
            }
        }
    }

    /** Reseta o estado para Idle */
    fun resetState() {
        _state.value = AddTransactionState.Idle
    }
}
