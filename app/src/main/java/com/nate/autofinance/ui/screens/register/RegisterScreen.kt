package com.nate.autofinance.ui.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nate.autofinance.R
import com.nate.autofinance.ui.components.AppLargeCenteredTopBar
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun RegisterScreenVisual() {
    // Estados locais para os campos de cadastro, loading e mensagem de erro
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // Use um TopBar semelhante, ajustando o título para "Criar Conta"
            AppLargeCenteredTopBar(stringResource(id = R.string.create_account))
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crie sua conta e comece a organizar suas finanças!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            RegisterFields(
                name = name,
                onNameChange = { name = it },
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it },
                errorMessage = errorMessage
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = ""
                    // Validação dos campos
                    if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                        errorMessage = "Preencha todos os campos!"
                        isLoading = false
                    } else if (password != confirmPassword) {
                        errorMessage = "As senhas não coincidem!"
                        isLoading = false
                    } else {
                        // Chame aqui sua função de cadastro
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Cadastrar")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { /* Navegar para a tela de login */ }) {
                Text("Já possui uma conta? Faça login")
            }
        }
    }
}
