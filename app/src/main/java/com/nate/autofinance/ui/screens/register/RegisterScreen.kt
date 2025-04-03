package com.nate.autofinance.ui.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nate.autofinance.R
import com.nate.autofinance.ui.components.AppLargeCenteredTopBar
import com.nate.autofinance.viewmodel.RegisterState
import com.nate.autofinance.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit, // Callback para navegação com mensagem
    onNavigateToLogin: () -> Unit,         // Callback para voltar manualmente para a tela de login
    viewModel: RegisterViewModel = viewModel()
) {
    // Observa o estado de registro exposto pelo ViewModel
    val registerState by viewModel.registerState.collectAsState()

    // Estados locais para os campos do formulário
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Define mensagem de erro a partir do estado, se houver
    val errorMessage = if (registerState is RegisterState.Error) {
        (registerState as RegisterState.Error).message
    } else {
        ""
    }

    // Indica se está em Loading
    val isLoading = registerState is RegisterState.Loading

    // Quando o registro for bem-sucedido, aciona o callback para navegar para a tela de login
    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Success) {
            onRegisterSuccess("Cadastro realizado com sucesso!")
        }
    }

    Scaffold(
        topBar = {
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
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Composable com os campos de cadastro (RegisterFields)
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
                onClick = { viewModel.register(name, email, password, confirmPassword) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Cadastrar")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            // Botão para voltar à tela de login
            TextButton(onClick = { onNavigateToLogin() }) {
                Text("Já possui uma conta? Faça login")
            }
        }
    }
}
