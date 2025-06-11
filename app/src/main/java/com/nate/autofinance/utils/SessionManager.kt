package com.nate.autofinance.utils

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private const val PREFS_NAME = "auto_finance_prefs"
    private const val KEY_USER_ID = "KEY_USER_ID"
    private const val KEY_SELECTED_PERIOD_ID = "KEY_SELECTED_PERIOD_ID"

    // StateFlow para observar mudanças no período selecionado
    private val _selectedPeriodIdFlow = MutableStateFlow<Int?>(null)
    val selectedPeriodIdFlow: StateFlow<Int?> = _selectedPeriodIdFlow.asStateFlow()

    private var isInitialized = false

    /** Persiste o ID do usuário logado (Room) */
    fun saveUserId(context: Context, userId: Int?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(KEY_USER_ID, userId ?: -1)
        }
        println("SessionManager: Usuário salvo: $userId")
    }

    /** Recupera o ID do usuário logado, ou null se não houver */
    fun getUserId(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_USER_ID, -1)
        val userId = if (id >= 0) id else null
        println("SessionManager: Usuário recuperado: $userId")
        return userId
    }

    /** Persiste o ID do período financeiro atualmente selecionado */
    fun saveSelectedPeriodId(context: Context, periodId: Int?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(KEY_SELECTED_PERIOD_ID, periodId ?: -1)
        }
        // Atualiza o StateFlow
        _selectedPeriodIdFlow.value = periodId
        println("SessionManager: Período selecionado salvo: $periodId")
    }

    /** Recupera o ID do período financeiro selecionado, ou null se não houver */
    fun getSelectedPeriodId(context: Context): Int? {
        initializeIfNeeded(context)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_SELECTED_PERIOD_ID, -1)
        val periodId = if (id >= 0) id else null

        println("SessionManager: Período selecionado recuperado: $periodId")
        return periodId
    }

    /** Inicializa o StateFlow com o valor das preferências se ainda não foi inicializado */
    private fun initializeIfNeeded(context: Context) {
        if (!isInitialized) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val id = prefs.getInt(KEY_SELECTED_PERIOD_ID, -1)
            val periodId = if (id >= 0) id else null
            _selectedPeriodIdFlow.value = periodId
            isInitialized = true
            println("SessionManager: Inicializado com período: $periodId")
        }
    }

    /** Limpa todas as preferências da sessão (usuário e período selecionado) */
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                clear()
            }
        _selectedPeriodIdFlow.value = null
        isInitialized = false
        println("SessionManager: Sessão limpa")
    }
}