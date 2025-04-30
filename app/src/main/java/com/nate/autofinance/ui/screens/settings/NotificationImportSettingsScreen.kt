package com.nate.autofinance.ui.screens.settings

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import com.nate.autofinance.ui.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationImportSettingsScreen(
    onBack: () -> Unit,
    onSubscribe: () -> Unit,

) {
    // **Coleta de estado**
    val app = LocalContext.current.applicationContext as Application

    val viewModel: SubscriptionViewModel = viewModel(
        modelClass = SubscriptionViewModel::class.java,
        factory = AndroidViewModelFactory.getInstance(app)
    )

    val isSubscribed by viewModel.isSubscribed.collectAsState()
    val isReadingEnabled by viewModel.isReadingEnabled.collectAsState()

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
                // Usuário não é premium ainda
                Text(
                    "Este recurso está disponível somente para usuários Premium.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = onSubscribe,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Torne-se Premium", color = Color.White)
                }

            } else {
                // Usuário Premium: exibe switch para habilitar leitura
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    },
                    headlineContent = {
                        Text("Importação de notificações")
                    },
                    supportingContent = {
                        Text("Permite importar automaticamente notificações de transações bancárias.")
                    },
                    trailingContent = {
                        Switch(
                            checked = isReadingEnabled,
                            onCheckedChange = { _ ->
                                // Abre configurações de acessibilidade/notificações
                                viewModel.enableNotificationReading()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ListItemDefaults.colors(
                        headlineColor = MaterialTheme.colorScheme.onSurface,
                        leadingIconColor = MaterialTheme.colorScheme.primary
                    )
                )

                if (!isReadingEnabled) {
                    Text(
                        "Para ativar, abra as configurações de notificações do sistema e habilite o AutoFinance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
