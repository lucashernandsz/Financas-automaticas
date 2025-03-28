import CategoryFilterRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.nate.autofinance.R
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMenuScreen(
    onBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToAppSettings: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val isPremium = false

    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Configurações",
                showBackButton = true,
                onBackButtonClick = onBack
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
            SettingsItem(Icons.Default.Person, "Informações pessoais", enabled = true, onClick = onNavigateToProfile)
            //SettingsItem(Icons.Default.Build, "Contas conectadas", enabled = true, onClick = onNavigateToAccounts)
            SettingsItem(Icons.Default.Settings, "Configurações do aplicativo", enabled = true, onClick = onNavigateToAppSettings)
            SettingsItem(Icons.Default.AddCircle, "Gerenciar categorias", enabled = isPremium, onClick = onNavigateToCategories)
            SettingsItem(Icons.Default.Notifications, "Inserção automática por notificação", enabled = isPremium, onClick = onNavigateToNotifications)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { /* TODO: upgrade flow */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Torne‑se premium", color = Color.White)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Iniciar novo período financeiro", color = Color.White)
            }

            OutlinedButton(
                onClick = { /* TODO */ },
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

