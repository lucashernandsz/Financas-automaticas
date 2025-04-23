package com.nate.autofinance.ui.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nate.autofinance.R
import com.nate.autofinance.ui.components.AppLargeCenteredTopBar
import com.nate.autofinance.viewmodel.RegisterState
import com.nate.autofinance.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    // 1) Observa o estado de registro
    val state by viewModel.registerState.collectAsState()

    // 2) Campos do formulário
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // 3) SnackHost para mensagens de erro
    val snackbarHostState = remember { SnackbarHostState() }

    // 4) Side‑effects para reagir a mudanças de estado
    LaunchedEffect(state) {
        when (state) {
            is RegisterState.Error -> {
                // exibe o erro em um snackbar
                val message = (state as RegisterState.Error).message
                snackbarHostState.showSnackbar(message)
            }
            is RegisterState.Success -> {
                // navega para login com mensagem
                onRegisterSuccess("Cadastro realizado com sucesso!")
            }
            else -> { /* Idle ou Loading: nada a fazer */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppLargeCenteredTopBar(stringResource(R.string.create_account))
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirme a senha") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.register(name, email, password, confirmPassword) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is RegisterState.Loading
            ) {
                if (state is RegisterState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Cadastrar")
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onNavigateToLogin) {
                Text("Já possui conta? Entrar")
            }
        }
    }
}
