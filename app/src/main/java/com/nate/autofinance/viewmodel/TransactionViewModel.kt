// app/src/main/java/com/nate/autofinance/viewmodel/TransactionViewModel.kt
package com.nate.autofinance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.utils.Categories
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel : ViewModel() {

    // pega o use case do ServiceLocator
    private val fetchTransactionsForSelectedPeriod =
        ServiceLocator.fetchTransactionsForSelectedPeriodUseCase

    private val _categories = MutableStateFlow(listOf("All") + Categories.fixedCategories)
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())

    // combina lista + filtro
    val filteredTransactions: StateFlow<List<Transaction>> =
        combine(_transactions, _selectedCategory) { txs, cat ->
            when (cat) {
                "All" -> txs
                Categories.INCOME -> txs.filter { it.category == Categories.INCOME }
                else -> txs.filter { it.category == cat }
            }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    /** Carrega as transações do período ativo do usuário */
    fun loadTransactions() {
        viewModelScope.launch {
            _transactions.value = try {
                fetchTransactionsForSelectedPeriod()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /** Atualiza o filtro de categoria */
    fun setCategoryFilter(category: String) {
        if (_categories.value.contains(category)) {
            _selectedCategory.value = category
        }
    }
}
