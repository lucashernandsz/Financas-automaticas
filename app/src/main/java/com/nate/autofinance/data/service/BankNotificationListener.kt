package com.nate.autofinance.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.data.categorization.Categorizer
import com.nate.autofinance.data.categorization.PaymentTypeDetector
import com.nate.autofinance.data.repository.SettingsRepository
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.utils.Categories
import com.nate.autofinance.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!enabledImport) return

        val pkg = sbn.packageName
        val text = sbn.notification.extras
            .getString("android.text") ?: return

        // **Log para debug**
        Log.d("BankNotifListener", "Recebeu notificação: pacote=$pkg, texto=$text")

        // Filtra por pacotes de bancos conhecidos
        if (pkg.contains("nubank", true) || pkg.contains("itau", true) || pkg.contains("inter", true) || pkg.contains("autofinance", true)) {
            if(text.contains("você acaba de comprar", true)){
                processBankNotification(text)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processBankNotification(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1) Pega o valor (ex: "R$ 1.234,56")
                val raw = Regex("""R\$ ?([\d.,]+)""")
                    .find(text)
                    ?.groupValues
                    ?.get(1)
                    ?: return@launch

                val value = raw
                    .replace(".", "")
                    .replace(",", ".")
                    .toDoubleOrNull()
                    ?: return@launch

                val amount = -value

                // 3) Pega o usuário logado
                val userId = SessionManager
                    .getUserId(applicationContext)
                    ?: return@launch

                val category = Categorizer.categorize(text)
                val isCredit = PaymentTypeDetector.isCredit(text)

                // 5) Cria e salva a transação na categoria “Other”
                val tx = Transaction(
                    date = Date(),
                    amount = amount,
                    description = text,
                    category = category,
                    userId = userId,
                    isCredit = isCredit,
                    financialPeriodId = 0, // Será ajustado depois
                    imported = true
                )
                ServiceLocator.addTransactionUseCase.invoke(tx)

            } catch (e: Exception) {
                Log.e("BankNotifListener", "Erro ao processar notificação", e)
            }
        }
    }


    private fun createNotificationChannel() {
        // Apenas em Android O (API 26) ou superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
