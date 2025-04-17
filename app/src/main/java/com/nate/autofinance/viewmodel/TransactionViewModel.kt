// app/src/main/java/com/nate/autofinance/viewmodel/TransactionViewModel.kt
package com.nate.autofinance.viewmodel

import FetchTransactionsForSelectedPeriodUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.utils.Categories
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val fetchTransactionsForSelectedPeriod: FetchTransactionsForSelectedPeriodUseCase
) : ViewModel() {

    // 1) Lista de categorias, com "All" na primeira posição
    private val _categories = MutableStateFlow<List<String>>(
        listOf("All") + Categories.fixedCategories
    )
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // 2) Categoria selecionada (inicialmente "All")
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 3) Lista bruta de transações
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // 4) Transações filtradas combinando ambas
    val filteredTransactions: StateFlow<List<Transaction>> =
        combine(_transactions, _selectedCategory) { txs, cat ->
            filterTransactions(txs, cat)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    /** Carrega as transações do período ativo do usuário */
    fun loadTransactions() {
        viewModelScope.launch {
            try {
                val list = fetchTransactionsForSelectedPeriod()
                _transactions.value = list
            } catch (e: Exception) {
                // trate o erro ou exponha via outro StateFlow
            }
        }
    }

    /** Atualiza o filtro de categoria */
    fun setCategoryFilter(category: String) {
        if (_categories.value.contains(category)) {
            _selectedCategory.value = category
        }
    }

    /** Aplica o filtro usando as constantes de Categories */
    private fun filterTransactions(
        transactions: List<Transaction>,
        category: String
    ): List<Transaction> = when (category) {
        "All" -> transactions
        Categories.INCOME         -> transactions.filter { it.amount >= 0 }
        Categories.FOOD,
        Categories.ENTERTAINMENT,
        Categories.TRANSPORTATION,
        Categories.EDUCATION,
        Categories.HEALTH,
        Categories.HOUSING,
        Categories.OTHER           ->
            transactions.filter { it.category == category }
        else -> transactions
    }
}
