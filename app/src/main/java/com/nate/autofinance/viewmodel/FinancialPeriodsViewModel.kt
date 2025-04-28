package com.nate.autofinance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.domain.models.FinancialPeriod
import com.nate.autofinance.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciamento de períodos financeiros.
 * Dependências são obtidas via ServiceLocator, permitindo instanciação sem parâmetros.
 */
class FinancialPeriodsViewModel : ViewModel() {
    private val getAllPeriodsForUserUseCase = ServiceLocator.getAllPeriodsForUserUseCase
    private val selectPeriodUseCase = ServiceLocator.selectPeriodUseCase
    private val deletePeriodsUseCase = ServiceLocator.deletePeriodsUseCase

    private val _periods = MutableStateFlow<List<FinancialPeriod>>(emptyList())
    val periods: StateFlow<List<FinancialPeriod>> = _periods.asStateFlow()

    private val _selectedIndices = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIndices: StateFlow<Set<Int>> = _selectedIndices.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Carrega todos os períodos do usuário logado */
    fun loadPeriods() {
        viewModelScope.launch {
            _periods.value = getAllPeriodsForUserUseCase()
        }
    }

    /** Seleciona/deseleciona um índice na lista */
    fun toggleSelection(index: Int) {
        _selectedIndices.value = _selectedIndices.value
            .let { if (it.contains(index)) it - index else it + index }
    }

    /** Marca o período como corrente */
    fun selectAsCurrent(index: Int) {
        val period = _periods.value.getOrNull(index) ?: return
        viewModelScope.launch {
            selectPeriodUseCase(period.id)
            loadPeriods()
        }
    }

    /** Exclui os períodos selecionados */
    fun deleteSelected() {
        val toDelete = _selectedIndices.value
            .mapNotNull { _periods.value.getOrNull(it) }
        viewModelScope.launch {
            try {
                deletePeriodsUseCase(toDelete)
                _selectedIndices.value = emptySet()
                loadPeriods()
            } catch (e: IllegalStateException) {
                _errorMessage.value = e.message
            }
        }
    }

    /** Limpa mensagens de erro */
    fun clearError() {
        _errorMessage.value = null
    }
}