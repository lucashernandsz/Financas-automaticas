package com.nate.autofinance.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1) Define o DataStore de Preferences no Context
private val Context.dataStore by preferencesDataStore(name = "settings")

// 2) Chaves usadas nas Preferences
object SettingsPreferences {
    val NOTIFICATION_IMPORT_ENABLED = booleanPreferencesKey("notification_import_enabled")
}

// 3) Repository para ler/escrever a configuração de importação de notificações
class SettingsRepository(private val context: Context) {

    // Fluxo que emite true/false conforme o usuário habilitou ou não
    val notificationImportEnabled: Flow<Boolean> =
        context.dataStore.data
            .map { prefs ->
                prefs[SettingsPreferences.NOTIFICATION_IMPORT_ENABLED] ?: false
            }

    // Grava a preferência (habilitar/desabilitar)
    suspend fun setNotificationImportEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SettingsPreferences.NOTIFICATION_IMPORT_ENABLED] = enabled
        }
    }
}
