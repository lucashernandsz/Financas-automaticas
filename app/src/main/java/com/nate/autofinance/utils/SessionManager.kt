package com.nate.autofinance.utils

import android.content.Context
import androidx.core.content.edit

object SessionManager {
    private const val PREFS_NAME = "auto_finance_prefs"
    private const val KEY_USER_ID = "KEY_USER_ID"
    private const val KEY_SELECTED_PERIOD_ID = "KEY_SELECTED_PERIOD_ID"

    /** Persiste o ID do usuário logado (Room) */
    fun saveUserId(context: Context, userId: Int?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(KEY_USER_ID, userId ?: -1)
        }
    }

    /** Recupera o ID do usuário logado, ou null se não houver */
    fun getUserId(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_USER_ID, -1)
        return if (id >= 0) id else null
    }

    /** Persiste o ID do período financeiro atualmente selecionado */
    fun saveSelectedPeriodId(context: Context, periodId: Int?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(KEY_SELECTED_PERIOD_ID, periodId ?: -1)
        }
    }

    /** Recupera o ID do período financeiro selecionado, ou null se não houver */
    fun getSelectedPeriodId(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_SELECTED_PERIOD_ID, -1)
        return if (id >= 0) id else null
    }

    /** Limpa todas as preferências da sessão (usuário e período selecionado) */
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                clear()
            }
    }
}
