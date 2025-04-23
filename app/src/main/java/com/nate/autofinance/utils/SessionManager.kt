package com.nate.autofinance.utils

import android.content.Context
import androidx.core.content.edit

object SessionManager {
    private const val PREFS_NAME = "auto_finance_prefs"
    private const val KEY_USER_ID = "KEY_USER_ID"

    fun saveUserId(context: Context, userId: Int?) {
        val prefs = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit() {
            putInt(KEY_USER_ID, userId ?: -1)
        }
    }

    fun getUserId(context: Context): Int? {
        val prefs = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_USER_ID, -1)
        return if (id >= 0) id else null
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit() {
                clear()
            }
    }
}
