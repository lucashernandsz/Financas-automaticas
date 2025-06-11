// TransactionViewModel.kt
package com.nate.autofinance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.utils.Categories
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel : ViewModel() {

    private val repo = ServiceLocator.transactionRepository
    private val session = ServiceLocator.sessionManager
    private val ctx = ServiceLocator.context

    // 1) StateFlow interno para controlar o período selecionado
    private val _selectedPeriodId = MutableStateFlow<Int?>(null)

    // 2) Combina o StateFlow interno com mudanças do SessionManager
    private val selectedPeriodIdFlow: StateFlow<Int?> =
        combine(_selectedPeriodId, session.selectedPeriodIdFlow) { internal, session ->
            session ?: internal
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    // 3) Em cada mudança de período, troca para o fluxo de transações reativo
    private val transactionsFlow: Flow<List<Transaction>> =
        selectedPeriodIdFlow.flatMapLatest { periodId ->
            periodId?.let {
                println("TransactionViewModel: Observando transações para período $it")
                repo.observeTransactions(it)
            } ?: flowOf(emptyList())
        }

    // 4) Transforma em StateFlow para a UI consumir
    val transactions: StateFlow<List<Transaction>> =
        transactionsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // —— filtros de categoria ——
    private val _categories = MutableStateFlow(listOf("All") + Categories.fixedCategories)
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredTransactions: StateFlow<List<Transaction>> =
        combine(transactions, selectedCategory) { txs, cat ->
            println("TransactionViewModel: Filtrando ${txs.size} transações para categoria '$cat'")
            when {
                cat == "All" -> txs
                cat == Categories.INCOME -> txs.filter { it.category == Categories.INCOME }
                else -> txs.filter { it.category == cat }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Carrega período selecionado na inicialização
        loadSelectedPeriod()

        // Observa mudanças no período através do SessionManager
        viewModelScope.launch {
            session.selectedPeriodIdFlow.collect { periodId ->
                if (periodId != null && periodId != _selectedPeriodId.value) {
                    println("TransactionViewModel: Período selecionado mudou para $periodId")
                    _selectedPeriodId.value = periodId
                }
            }
        }
    }

    private fun loadSelectedPeriod() {
        viewModelScope.launch {
            val periodId = session.getSelectedPeriodId(ctx)
            println("TransactionViewModel: Carregando período selecionado: $periodId")
            _selectedPeriodId.value = periodId
        }
    }

    /** Atualiza filtro de categoria */
    fun setCategoryFilter(category: String) {
        if (_categories.value.contains(category)) {
            _selectedCategory.value = category
        }
    }

    /** Força reload do período selecionado */
    fun refreshPeriod() {
        loadSelectedPeriod()
    }
}