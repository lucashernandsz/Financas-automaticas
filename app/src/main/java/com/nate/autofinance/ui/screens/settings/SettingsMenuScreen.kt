package com.nate.autofinance.ui.screens.settings

import android.app.Application
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
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
    // 1) Pega o Application e injeta o SubscriptionViewModel via AndroidViewModelFactory
    val app = LocalContext.current.applicationContext as Application
    val viewModel: SubscriptionViewModel = viewModel(
        modelClass = SubscriptionViewModel::class.java,
        factory = AndroidViewModelFactory.getInstance(app)
    )

    // 2) Coleta via collectAsState o StateFlow isSubscribed
    val isSubscribed by viewModel.isSubscribed.collectAsState()

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
            // Agora habilita somente se o usuário for assinante
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

            // Botão de upgrade aparece apenas se não for assinante
            if (!isSubscribed) {
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
            }

            // Esses botões continuam visíveis independentemente da assinatura
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
            headlineColor     = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray,
            leadingIconColor  = if (enabled) MaterialTheme.colorScheme.primary   else Color.Gray
        )
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsMenuScreenPreview() {
    SettingsMenuScreen(
        onNavigateToCategories = {},
        onNavigateToNotifications = {},
        onNavigateToPremium = {},
        onNavigateToNewFinancialPeriod = {},
        onNavigateToFinancialPeriods = {}
    )
}
