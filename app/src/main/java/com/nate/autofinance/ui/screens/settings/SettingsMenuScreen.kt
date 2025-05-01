package com.nate.autofinance.ui.screens.settings

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nate.autofinance.R
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import com.nate.autofinance.ui.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMenuScreen(
    onBack: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onNavigateToNewFinancialPeriod: () -> Unit = {},
    onNavigateToFinancialPeriods: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as android.app.Application
    val viewModel: SubscriptionViewModel = viewModel(
        modelClass = SubscriptionViewModel::class.java,
        factory = AndroidViewModelFactory.getInstance(app)
    )
    val isSubscribed by viewModel.isSubscribed.collectAsState()
    val context = LocalContext.current

    // Launcher para pedir POST_NOTIFICATIONS em Android 13+
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) simulateTestNotification(context)
    }

    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Configurações",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsItem(
                icon = Icons.Default.AddCircle,
                text = "Gerenciar categorias",
                enabled = isSubscribed,
                onClick = onNavigateToCategories
            )
            SettingsItem(
                icon = Icons.Default.Notifications,
                text = "Inserção automática por notificação",
                enabled = isSubscribed,
                onClick = onNavigateToNotifications
            )

            Spacer(Modifier.height(24.dp))

            // Botão Premium sempre visível
            Button(
                onClick = onNavigateToPremium,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Torne-se premium", color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onNavigateToNewFinancialPeriod,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Iniciar novo período financeiro", color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onNavigateToFinancialPeriods,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Navegar entre períodos financeiros")
            }

            // DEV: Simulador de notificação
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            simulateTestNotification(context)
                        } else {
                            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        simulateTestNotification(context)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Simular notificação de compra (DEV)")
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        leadingContent = { Icon(icon, contentDescription = null) },
        headlineContent = { Text(text) },
        trailingContent = {
            if (!enabled) Icon(Icons.Default.Lock, contentDescription = "Bloqueado")
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        colors = ListItemDefaults.colors(
            headlineColor    = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray,
            leadingIconColor = if (enabled) MaterialTheme.colorScheme.primary   else Color.Gray
        )
    )
}

private fun simulateTestNotification(context: Context) {
    val channelId = "autofinance_channel"

    // 1) Cria canal se necessário (Android O+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mgr.getNotificationChannel(channelId) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "AutoFinance Ativo",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Canal para notificações de teste"
                }
            )
        }
    }

    // 2) Monta notificação COM ícone, título, texto e prioridade
    val notification = try {
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)           // ícone obrigatório
            .setContentTitle("Teste de notificação")
            .setContentText("Você gastou R$ 100 no supermercado Nova Rita")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
    } catch (e: Exception) {
        e.printStackTrace()
        return
    }

    // 3) Envia
    try {
        NotificationManagerCompat.from(context)
            .notify(999, notification)                         // use um ID > 0
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}
