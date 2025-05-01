package com.nate.autofinance.ui.screens.settings

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import com.nate.autofinance.ui.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationImportSettingsScreen(
    onBack: () -> Unit,
    onSubscribe: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application

    val viewModel: SubscriptionViewModel = viewModel(
        modelClass = SubscriptionViewModel::class.java,
        factory = AndroidViewModelFactory.getInstance(app)
    )

    val isSubscribed by viewModel.isSubscribed.collectAsState()
    val isImportEnabled by viewModel.isImportEnabled.collectAsState()
    val hasPermission by viewModel.hasPermission.collectAsState()

    // launcher para permissões de notificação no Android 13+
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.enableNotificationReading()
    }

    // Recarrega permissão sempre que a tela voltar ao foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Importação de Notificações",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isSubscribed) {
                Text(
                    "Este recurso é só para usuários Premium.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = onSubscribe, Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Torne-se Premium")
                }
            } else {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    headlineContent = { Text("Importar notificações") },
                    supportingContent = {
                        Text("Ative para ler notificações de transações.")
                    },
                    trailingContent = {
                        Switch(
                            checked = isImportEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.toggleImportEnabled(enabled)
                                if (enabled) {
                                    // abre settings, pedindo permissão se Android 13+
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            viewModel.enableNotificationReading()
                                        } else {
                                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    } else {
                                        viewModel.enableNotificationReading()
                                    }
                                }
                            }
                        )
                    }
                )
                if (isImportEnabled && !hasPermission) {
                    Text(
                        "Por favor, habilite o acesso em Configurações → Acesso a notificações.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
