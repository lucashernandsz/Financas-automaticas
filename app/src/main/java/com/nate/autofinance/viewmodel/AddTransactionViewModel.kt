package com.nate.autofinance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.domain.usecases.transaction.AddTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Define os estados possíveis para a ação de adicionar uma transação.
sealed class AddTransactionState {
    object Idle : AddTransactionState()       // Estado inicial ou inativo
    object Loading : AddTransactionState()    // Em processamento
    object Success : AddTransactionState()    // Transação adicionada com sucesso
    data class Error(val message: String) : AddTransactionState() // Erro ocorrido ao adicionar
}

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    // StateFlow para emitir os estados da operação para a UI.
    private val _state = MutableStateFlow<AddTransactionState>(AddTransactionState.Idle)
    val state: StateFlow<AddTransactionState> = _state

    // Função que recebe o objeto Transaction e invoca o caso de uso para adicioná-la.
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _state.value = AddTransactionState.Loading
            try {
                addTransactionUseCase(transaction)
                _state.value = AddTransactionState.Success
            } catch (e: Exception) {
                _state.value = AddTransactionState.Error(e.message ?: "Erro desconhecido ao adicionar transação.")
            }
        }
    }
}
