package com.nate.autofinance.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nate.autofinance.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BankNotificationListener : NotificationListenerService() {

    companion object {
        private const val CHANNEL_ID = "autofinance_channel"
        private const val ONGOING_NOTIFICATION_ID = 1001
    }

    // Controla se devemos processar notificações
    private var enabledImport = false

    override fun onCreate() {
        super.onCreate()
        // 1) Garante que o canal existe (API ≥ 26)
        createNotificationChannel()

        // 2) Coleta a preferência de leitura em background
        val repo = SettingsRepository(applicationContext)
        CoroutineScope(Dispatchers.Default).launch {
            repo.notificationImportEnabled.collect { enabled ->
                enabledImport = enabled
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("BankNotifListener", "=== Listener CONNECTED === enabledImport=$enabledImport")
        if (enabledImport) {
            val notif = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("AutoFinance ativo")
                .setContentText("Importação de notificações habilitada")
                .setOngoing(true)
                .build()

            startForeground(ONGOING_NOTIFICATION_ID, notif)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("BankNotifListener", "=== Listener DISCONNECTED ===")
        // Sai do modo foreground e remove notificação
        stopForeground(true)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!enabledImport) return

        val pkg = sbn.packageName
        val text = sbn.notification.extras
            .getString("android.text") ?: return

        // **Log para debug**
        Log.d("BankNotifListener", "Recebeu notificação: pacote=$pkg, texto=$text")

        // Filtra por pacotes de bancos conhecidos
        if (pkg.contains("nubank", true) || pkg.contains("itau", true)) {
            processBankNotification(text)
        }
    }

    private fun processBankNotification(text: String) {
        // TODO: implemente seu pipeline de parsing, classificação,
        // criação de Transaction e chame o AddTransactionUseCase
    }

    private fun createNotificationChannel() {
        // Apenas em Android O (API 26) ou superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "AutoFinance Ativo",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Serviço ativo lendo notificações bancárias"
                }
                mgr.createNotificationChannel(channel)
            }
        }
    }
}
