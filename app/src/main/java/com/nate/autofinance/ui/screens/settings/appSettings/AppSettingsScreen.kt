package com.nate.autofinance.ui.screens.settings.appSettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nate.autofinance.ui.components.AppTopBarPageTitle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit = {},
    onToggleDarkMode: (Boolean) -> Unit = {},
    onSaveAndBack: () -> Unit = {},
    onExit: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Configurações do aplicativo",
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Modo escuro", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSaveAndBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Salvar e voltar", color = Color.White)
            }

            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Sair")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppSettingsScreenPreview() {
    AppSettingsScreen(
        isDarkMode = false,
        onBack = {},
        onToggleDarkMode = {},
        onSaveAndBack = {},
        onExit = {}
    )
}
