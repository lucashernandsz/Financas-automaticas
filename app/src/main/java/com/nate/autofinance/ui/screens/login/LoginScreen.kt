package com.nate.autofinance.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nate.autofinance.R
import com.nate.autofinance.ui.components.AppLargeCenteredTopBar
import com.nate.autofinance.viewmodel.LoginState
import com.nate.autofinance.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    snackbarMessage: String? = null
) {
    // injeta o ViewModel
    val viewModel: LoginViewModel = viewModel()
    // observa o estado (Idle / Loading / Success / Error)
    val state by viewModel.loginState.collectAsState()

    // campos de input
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // host para exibir Snackbars vindas de outras telas
    val snackbarHostState = remember { SnackbarHostState() }

    // se veio mensagem por parâmetro, exibe uma única vez
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    // ao sucesso de login, dispara a navegação
    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            onLoginSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { AppLargeCenteredTopBar(stringResource(R.string.welcome_message)) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Estamos comprometidos em te ajudar a organizar suas finanças.\nVamos começar?",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Campos de email e senha
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // botão de login
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is LoginState.Loading
            ) {
                if (state is LoginState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Entrar")
                }
            }

            // exibe erro localmente, se houver
            if (state is LoginState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = (state as LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.weight(1f))

            // link para registro
            TextButton(onClick = onNavigateToRegister) {
                Text("Não possui uma conta? Cadastre‑se")
            }
        }
    }
}
