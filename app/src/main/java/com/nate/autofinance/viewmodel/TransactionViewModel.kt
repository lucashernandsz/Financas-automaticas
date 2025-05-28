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
    private val ctx     = ServiceLocator.context

    // 1) Flow que pega o selectedPeriodId ou null
    private val selectedPeriodIdFlow: Flow<Int?> = flow {
        emit(session.getSelectedPeriodId(ctx))
    }

    // 2) Flow reativo de transações, vazio se não houver período
    private val transactionsFlow: Flow<List<Transaction>> =
        selectedPeriodIdFlow.flatMapLatest { periodId ->
            periodId?.let { repo.observeTransactions(it) }
                ?: flowOf(emptyList())
        }

    // 3) StateFlow para a UI consumir
    val transactions: StateFlow<List<Transaction>> =
        transactionsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // 4) Categorias disponíveis e filtro
    private val _categories = MutableStateFlow(listOf("All") + Categories.fixedCategories)
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 5) Lista filtrada por categoria
    val filteredTransactions: StateFlow<List<Transaction>> =
        combine(transactions, selectedCategory) { txs, cat ->
            when {
                cat == "All"             -> txs
                cat == Categories.INCOME -> txs.filter { it.category == Categories.INCOME }
                else                     -> txs.filter { it.category == cat }
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /** Atualiza o filtro de categoria */
    fun setCategoryFilter(category: String) {
        if (_categories.value.contains(category)) {
            _selectedCategory.value = category
        }
    }
}
