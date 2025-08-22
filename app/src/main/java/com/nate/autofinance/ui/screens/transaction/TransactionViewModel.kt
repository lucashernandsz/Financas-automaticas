// app/src/main/java/com/nate/autofinance/viewmodel/TransactionViewModel.kt
package com.nate.autofinance.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.utils.Categories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel : ViewModel() {

    private val repo = ServiceLocator.transactionRepository
    private val periodDao = ServiceLocator.financialPeriodDao
    private val session = ServiceLocator.sessionManager
    private val context = ServiceLocator.context
    private val syncManager = ServiceLocator.syncManager
    private val createDefaultPeriod = ServiceLocator.createDefaultPeriodUseCase

    private val userId = session.getUserId(context) ?: throw IllegalStateException("User ID is null")

    val selectedPeriodIdFlow: StateFlow<Long?> =
        periodDao.observeSelectedId(userId)
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000), null)

    private val transactionsFlow: Flow<List<Transaction>> =
        selectedPeriodIdFlow.flatMapLatest { periodId ->
            if (periodId != null) {
                repo.observeTransactions(periodId.toInt())
                    .catch { e ->
                        println("TransactionViewModel: ❌ Erro no fluxo: ${e.message}")
                        emit(emptyList())
                    }
            } else {
                println("TransactionViewModel: ⚠️ Período é null, emitindo lista vazia")
                flowOf(emptyList())
            }
        }

    val transactions: StateFlow<List<Transaction>> =
        transactionsFlow.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000), emptyList())

    private val _categories =
        MutableStateFlow(listOf("Todas") + Categories.fixedCategories.map { it.name })
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredTransactions: StateFlow<List<Transaction>> =
        combine(transactions, selectedCategory) { txs, cat ->
            println("TransactionViewModel: ✅ Filtrando ${txs.size} transações para '$cat'")
            val filtered = when {
                cat == "Todas" -> txs
                cat == Categories.Income.name -> txs.filter { it.category == Categories.Income.name }
                else -> txs.filter { it.category == cat }
            }
            println("TransactionViewModel: ✅ ${filtered.size} após filtragem")
            filtered
        }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setCategoryFilter(category: String) {
        if (_categories.value.contains(category)) {
            _selectedCategory.value = category
        }
    }

    fun getCategoryIcon(category: String): Int {
        return Categories.fixedCategories.find { it.name == category }?.iconResId
            ?: Categories.fixedCategories.firstOrNull { it.name == "Outros" }?.iconResId
            ?: Categories.Income.iconResId
    }

    fun clearError() { _errorMessage.value = null }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                syncManager.syncAll()

                val selectedIdNow: Long? = periodDao.observeSelectedId(userId).first()
                if (selectedIdNow == null) {
                    val periods = ServiceLocator.periodRepository.getPeriodsForUser(userId)
                    if (periods.isEmpty()) {
                        createDefaultPeriod()
                    } else if (periods.size == 1) {
                        ServiceLocator.periodRepository.selectOnly(periods.first().id)
                        session.saveSelectedPeriodId(context, periods.first().id.toInt())
                    } else {
                        ServiceLocator.periodRepository.selectOnly(periods.first().id)
                        session.saveSelectedPeriodId(context, periods.first().id.toInt())
                    }

                }

                periodDao.observeSelectedId(userId).first()?.let { id ->
                    session.saveSelectedPeriodId(context, id.toInt())
                }

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun syncAfterLogin() {
        println("TransactionViewModel: 🔐 syncAfterLogin()")
        refreshTransactions()
    }

    override fun onCleared() {
        super.onCleared()
    }
}