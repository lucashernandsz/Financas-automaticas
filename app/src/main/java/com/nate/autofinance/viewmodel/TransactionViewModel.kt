// TransactionViewModel.kt
// Fonte: :contentReference[oaicite:1]{index=1}

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

    // 1) Fluxo simples que emite o período selecionado (pode ser null)
    private val selectedPeriodIdFlow: Flow<Int?> = flow {
        emit(session.getSelectedPeriodId(ctx))
    }

    // 2) Em cada mudança de período, troca para o fluxo de transações reativo
    private val transactionsFlow: Flow<List<Transaction>> =
        selectedPeriodIdFlow.flatMapLatest { periodId ->
            periodId?.let { repo.observeTransactions(it) }
                ?: flowOf(emptyList())
        }

    // 3) Transforma em StateFlow para a UI consumir
    val transactions: StateFlow<List<Transaction>> =
        transactionsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // —— filtros de categoria (mantidos igual ao original) ——
    private val _categories = MutableStateFlow(listOf("All") + Categories.fixedCategories)
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredTransactions: StateFlow<List<Transaction>> =
        combine(transactions, selectedCategory) { txs, cat ->
            when {
                cat == "All"             -> txs
                cat == Categories.INCOME -> txs.filter { it.category == Categories.INCOME }
                else                     -> txs.filter { it.category == cat }
            }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Atualiza filtro de categoria */
    fun setCategoryFilter(category: String) {
        if (_categories.value.contains(category)) {
            _selectedCategory.value = category
        }
    }
}
