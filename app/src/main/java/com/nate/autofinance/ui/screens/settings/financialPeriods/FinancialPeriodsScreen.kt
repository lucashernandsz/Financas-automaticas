package com.nate.autofinance.ui.screens.settings.financialPeriods

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import com.nate.autofinance.utils.toLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialPeriodsScreen(
    viewModel: FinancialPeriodsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val periods by viewModel.periods.collectAsState()
    val activePeriodId by viewModel.activePeriodId.collectAsState()
    val activeIndex = periods.indexOfFirst { it.id == activePeriodId?.toInt() }
    val deleteSelection by viewModel.selectedIndices.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(Unit) { viewModel.loadPeriods() }

    val labels = periods.map { it.toLabel() }

    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Períodos financeiros" + if (deleteSelection.isNotEmpty()) " (${deleteSelection.size})" else "",
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
            labels.forEachIndexed { index, label ->
                val isActive = index == activeIndex
                val isSelectedForDelete = deleteSelection.contains(index)

                val background = when {
                    isActive -> Color.Black
                    isSelectedForDelete -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
                val contentColor = when {
                    isActive -> Color.White
                    isSelectedForDelete -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(deleteSelection.isEmpty()) {
                            detectTapGestures(
                                onTap = {
                                    if (deleteSelection.isNotEmpty()) {
                                        // Modo de exclusão: clique para selecionar/desselecionar
                                        if (!isActive) {
                                            viewModel.toggleSelection(index)
                                        }
                                    } else {
                                        // Modo normal: clique para definir como atual e voltar
                                        if (!isActive) {
                                            viewModel.selectAsCurrent(index)
                                        }
                                        onBack()
                                    }
                                },
                                onLongPress = {
                                    // Pressionar e segurar ativa o modo de exclusão
                                    if (deleteSelection.isEmpty() && !isActive) {
                                        viewModel.toggleSelection(index)
                                    }
                                }
                            )
                        },
                    colors = CardDefaults.cardColors(containerColor = background)
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
                            color = contentColor,
                            textAlign = TextAlign.Center
                        )
                        if (isSelectedForDelete) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 16.dp),
                                tint = contentColor
                            )
                        }
                    }
                }
            }

            if (deleteSelection.isNotEmpty()) {
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
