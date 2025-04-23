// app/src/main/java/com/nate/autofinance/viewmodel/EditTransactionViewModel.kt
package com.nate.autofinance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EditTransactionState {
    object Idle          : EditTransactionState()
    object Loading       : EditTransactionState()
    object UpdateSuccess: EditTransactionState()
    object DeleteSuccess: EditTransactionState()
    data class Error(val message: String) : EditTransactionState()
}

class EditTransactionViewModel : ViewModel() {

    private val getByIdUseCase    = ServiceLocator.getTransactionByIdUseCase
    private val editUseCase       = ServiceLocator.editTransactionUseCase
    private val deleteUseCase     = ServiceLocator.deleteTransactionUseCase

    // transação que será editada
    private val _transaction = MutableStateFlow<Transaction?>(null)
    val transaction: StateFlow<Transaction?> = _transaction

    // estado de loading / sucesso / erro
    private val _state = MutableStateFlow<EditTransactionState>(EditTransactionState.Idle)
    val state: StateFlow<EditTransactionState> = _state

    /** Busca a transação pelo ID e popula _transaction */
    fun loadTransaction(id: Int) {
        viewModelScope.launch {
            _transaction.value = try {
                getByIdUseCase(id)
            } catch (_: Exception) {
                null
            }
        }
    }

    /** Salva as alterações */
    fun editTransaction(tx: Transaction) {
        viewModelScope.launch {
            _state.value = EditTransactionState.Loading
            try {
                editUseCase(tx)
                _state.value = EditTransactionState.UpdateSuccess
            } catch (e: Exception) {
                _state.value = EditTransactionState.Error(e.message ?: "Erro ao atualizar")
            }
        }
    }

    /** Exclui a transação */
    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            _state.value = EditTransactionState.Loading
            try {
                deleteUseCase(tx)
                _state.value = EditTransactionState.DeleteSuccess
            } catch (e: Exception) {
                _state.value = EditTransactionState.Error(e.message ?: "Erro ao excluir")
            }
        }
    }
}
