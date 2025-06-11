// TransactionViewModel.kt
package com.nate.autofinance.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.utils.Categories
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel : ViewModel() {

    private val repo = ServiceLocator.transactionRepository
    private val session = ServiceLocator.sessionManager
    private val ctx = ServiceLocator.context
    private val syncManager = ServiceLocator.syncManager                       // :contentReference[oaicite:0]{index=0}
    private val createDefaultPeriod = ServiceLocator.createDefaultPeriodUseCase // :contentReference[oaicite:1]{index=1}

    // 1) Contém o período “interno” até que o SessionManager emita o valor
    private val _selectedPeriodId = MutableStateFlow<Int?>(null)

    // 2) Combina mudanças internas e do SessionManager
    private val selectedPeriodIdFlow: StateFlow<Int?> =
        combine(_selectedPeriodId, session.selectedPeriodIdFlow) { internal, remote ->
            val chosen = remote ?: internal
            println("TransactionViewModel: Período combinado - interno: $internal, session: $remote → $chosen")
            chosen
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    // 3) Muda de fluxo conforme o período selecionado
    private val transactionsFlow: Flow<List<Transaction>> =
        selectedPeriodIdFlow.flatMapLatest { periodId ->
            if (periodId != null) {
                println("TransactionViewModel: ✅ Observando transações para período $periodId")
                repo.observeTransactions(periodId)
                    .onStart { println("TransactionViewModel: 🔄 Iniciando observação das transações") }
                    .catch { e ->
                        println("TransactionViewModel: ❌ Erro no fluxo: ${e.message}")
                        emit(emptyList())
                    }
            } else {
                println("TransactionViewModel: ⚠️ Período é null, emitindo lista vazia")
                flowOf(emptyList())
            }
        }

    // 4) Exposto para a UI
    val transactions: StateFlow<List<Transaction>> =
        transactionsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // —— filtros de categoria ——
    private val _categories = MutableStateFlow(listOf("All") + Categories.fixedCategories)
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredTransactions: StateFlow<List<Transaction>> =
        combine(transactions, selectedCategory) { txs, cat ->
            println("TransactionViewModel: ✅ Filtrando ${txs.size} transações para '$cat'")
            val filtered = when {
                cat == "All"           -> txs
                cat == Categories.INCOME -> txs.filter { it.category == Categories.INCOME }
                else                   -> txs.filter { it.category == cat }
            }
            println("TransactionViewModel: ✅ ${filtered.size} após filtragem")
            filtered
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // —— estado de loading & erros ——
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        println("TransactionViewModel: Inicializando…")
        // Carrega de SharedPreferences → _selectedPeriodId
        loadSelectedPeriod()

        // Sempre que o SessionManager mudar, propaga para o interno
        viewModelScope.launch {
            session.selectedPeriodIdFlow.collect { remoteId ->
                if (remoteId != null && remoteId != _selectedPeriodId.value) {
                    println("TransactionViewModel: SessionManager emitiu novo período: $remoteId")
                    _selectedPeriodId.value = remoteId
                }
            }
        }
    }

    private fun loadSelectedPeriod() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val saved = session.getSelectedPeriodId(ctx)                  // :contentReference[oaicite:2]{index=2}
                println("TransactionViewModel: Carregado período inicial: $saved")
                _selectedPeriodId.value = saved
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar período: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Atualiza o filtro de categoria */
    fun setCategoryFilter(category: String) {
        if (_categories.value.contains(category)) {
            println("TransactionViewModel: Mudando filtro para $category")
            _selectedCategory.value = category
        }
    }

    /** Força recarregar o período salvo */
    fun refreshPeriod() = loadSelectedPeriod()

    /** Remove mensagem de erro */
    fun clearError() { _errorMessage.value = null }

    /**
     * Sincroniza tudo, garante que exista período e “refresh” das transações
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            println("TransactionViewModel: 🔄 Iniciando sincronização completa…")

            // 1️⃣ Sync remoto ↔ local
            try {
                syncManager.syncAll()
                println("TransactionViewModel: ✅ syncAll() OK")
            } catch (e: Exception) {
                println("TransactionViewModel: ⚠️ Erro no syncAll(): ${e.message}")
            }

            // 2️⃣ Se não houver período selecionado, cria/pergunta o padrão
            val curr = session.getSelectedPeriodId(ctx)
            if (curr == null) {
                println("TransactionViewModel: 🤖 Criando período padrão")
                try {
                    createDefaultPeriod()  // :contentReference[oaicite:3]{index=3}
                } catch (e: Exception) {
                    println("TransactionViewModel: ❌ Falha ao criar período padrão: ${e.message}")
                }
                val newId = session.getSelectedPeriodId(ctx)
                _selectedPeriodId.value = newId
                println("TransactionViewModel: Período agora = $newId")
            }

            // 3️⃣ Força re-emissão do Flow (pra garantir atualização da UI)
            _selectedPeriodId.value?.let { id ->
                _selectedPeriodId.value = null
                delay(100)
                _selectedPeriodId.value = id
            }

            _errorMessage.value = null
            _isLoading.value = false
            println("TransactionViewModel: ✅ refreshTransactions() concluído")
        }
    }

    /** Para uso imediato após login */
    @RequiresApi(Build.VERSION_CODES.O)
    fun syncAfterLogin() {
        println("TransactionViewModel: 🔐 syncAfterLogin()")
        refreshTransactions()
    }

    override fun onCleared() {
        super.onCleared()
        println("TransactionViewModel: onCleared()")
    }
}
