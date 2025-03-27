package com.nate.autofinance.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nate.autofinance.R
import com.nate.autofinance.ui.components.AppButton
import com.nate.autofinance.ui.components.AppLargeCenteredTopBar
import com.nate.autofinance.ui.theme.AutofinanceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LoginScreenVisual() {
    // Estados locais para email, senha, loading e mensagem de erro
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppLargeCenteredTopBar(stringResource(id = R.string.welcome_message))
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
                text = "Estamos comprometidos em te ajudar a organizar suas finanças. Vamos começar?\n",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            LoginFields(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                errorMessage = errorMessage
            )
            AppButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                text = "Entrar com o Google",
            )
            Button(
                onClick = {
                    // Simula uma ação de login
                    isLoading = true
                    errorMessage = ""
                    // Exemplo de validação:
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Preencha todos os campos!"
                        isLoading = false
                    } else {
                        // Simula um delay e sucesso
                        // Aqui você chamaria sua função de autenticação.
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
                    Text("Entrar")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { /* Navegar para tela de cadastro */ }) {
                Text("Não possui uma conta? Crie aqui")
            }
        }
    }
}