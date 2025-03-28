package com.nate.autofinance.ui.screens.settings.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nate.autofinance.ui.components.AppTopBarPageTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    email: String,
    password: String,
    onBack: () -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onSaveAndExit: () -> Unit = {},
    onExitWithoutSave: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Informações Pessoais",
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
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                trailingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = "Editar email")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = "Editar senha")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onSaveAndExit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Salvar alterações e sair", color = Color.White)
            }

            OutlinedButton(
                onClick = onExitWithoutSave,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Sair sem salvar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PersonalInfoScreenPreview() {
    PersonalInfoScreen(
        email = "user@example.com",
        password = "••••••••",
        onBack = {},
        onEmailChange = {},
        onPasswordChange = {},
        onSaveAndExit = {},
        onExitWithoutSave = {}
    )
}
