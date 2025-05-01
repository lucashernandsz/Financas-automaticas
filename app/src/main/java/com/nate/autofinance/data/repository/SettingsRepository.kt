package com.nate.autofinance.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1) Delegate para DataStore de Preferences
private val Context.dataStore by preferencesDataStore("settings")

object SettingsPreferences {
    val NOTIF_IMPORT_ENABLED = booleanPreferencesKey("notification_import_enabled")
}

class SettingsRepository(private val context: Context) {
    // 2) Fluxo que emite o valor salvo (default = false)
    val notificationImportEnabled: Flow<Boolean> =
        context.dataStore.data
            .map { prefs -> prefs[SettingsPreferences.NOTIF_IMPORT_ENABLED] ?: false }

    // 3) Grava a preferência
    suspend fun setNotificationImportEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SettingsPreferences.NOTIF_IMPORT_ENABLED] = enabled
        }
    }
}
