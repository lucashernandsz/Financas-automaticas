package com.nate.autofinance.ui.screens.settings.financialPeriods

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import com.nate.autofinance.utils.toLabel
import com.nate.autofinance.viewmodel.FinancialPeriodsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialPeriodsScreen(
    viewModel: FinancialPeriodsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    // 1) Observa o StateFlow do ViewModel
    val periods by viewModel.periods.collectAsState()
    val selected by viewModel.selectedIndices.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 2) Prepara o host para exibir Snackbars de erro
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // 3) Carrega os períodos quando o Composable aparecer
    LaunchedEffect(Unit) {
        viewModel.loadPeriods()
    }

    // 4) Transforma Period -> Label (“De 01/03/2024 até hoje • Saldo: R$ 250,00”)
    val labels = periods.map { it.toLabel() }

    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Períodos financeiros" + if (selected.isNotEmpty()) " (${selected.size})" else "",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 5) Lista de cartões
            labels.forEachIndexed { index, label ->
                val isSelected = selected.contains(index)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleSelection(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 16.dp)
                            )
                        }
                    }
                }
            }

            // 6) Botão de excluir aparece só se tiver seleção
            if (selected.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.deleteSelected() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Excluir", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}
