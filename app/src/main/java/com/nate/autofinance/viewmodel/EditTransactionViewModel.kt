package com.nate.autofinance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.domain.usecases.transaction.EditTransactionUseCase
import com.nate.autofinance.domain.usecases.transaction.DeleteTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estados distintos para atualização e exclusão.
sealed class EditTransactionState {
    object Idle : EditTransactionState()
    object Loading : EditTransactionState()
    object UpdateSuccess : EditTransactionState()
    object DeleteSuccess : EditTransactionState()
    data class Error(val message: String) : EditTransactionState()
}

class EditTransactionViewModel(
    private val editTransactionUseCase: EditTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<EditTransactionState>(EditTransactionState.Idle)
    val state: StateFlow<EditTransactionState> get() = _state

    fun editTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _state.value = EditTransactionState.Loading
            try {
                editTransactionUseCase(transaction)
                _state.value = EditTransactionState.UpdateSuccess
            } catch (e: Exception) {
                _state.value = EditTransactionState.Error(e.message ?: "Erro desconhecido ao editar transação")
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _state.value = EditTransactionState.Loading
            try {
                deleteTransactionUseCase(transaction)
                _state.value = EditTransactionState.DeleteSuccess
            } catch (e: Exception) {
                _state.value = EditTransactionState.Error(e.message ?: "Erro desconhecido ao deletar transação")
            }
        }
    }
}
