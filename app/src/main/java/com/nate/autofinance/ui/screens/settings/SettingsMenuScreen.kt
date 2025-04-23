package com.nate.autofinance.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.nate.autofinance.ui.components.AppTopBarPageTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMenuScreen(
    onBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAppSettings: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},               // Novo callback para a tela premium
    onNavigateToNewFinancialPeriod: () -> Unit = {},       // Novo callback para iniciar novo período
    onNavigateToFinancialPeriods: () -> Unit = {}          // Novo callback para navegar entre períodos
) {
    val isPremium = false

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
            //SettingsItem(Icons.Default.Person, "Informações pessoais", enabled = true, onClick = onNavigateToProfile)
            //SettingsItem(Icons.Default.Build, "Contas conectadas", enabled = true, onClick = onNavigateToAccounts)
            //SettingsItem(Icons.Default.Settings, "Configurações do aplicativo", enabled = true, onClick = onNavigateToAppSettings)
            SettingsItem(Icons.Default.AddCircle, "Gerenciar categorias", enabled = isPremium, onClick = onNavigateToCategories)
            SettingsItem(Icons.Default.Notifications, "Inserção automática por notificação", enabled = isPremium, onClick = onNavigateToNotifications)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onNavigateToPremium, // Navega para a tela de assinatura/premium
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Torne‑se premium", color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onNavigateToNewFinancialPeriod, // Navega para a tela de novo período financeiro
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Iniciar novo período financeiro", color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onNavigateToFinancialPeriods, // Navega para a tela de períodos financeiros
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
            headlineColor = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray,
            leadingIconColor = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray
        )
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsMenuScreenPreview() {
    MaterialTheme {
        SettingsMenuScreen()
    }
}
