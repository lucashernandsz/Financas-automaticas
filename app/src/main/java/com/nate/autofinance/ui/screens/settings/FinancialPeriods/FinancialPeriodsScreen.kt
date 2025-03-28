package com.nate.autofinance.ui.screens.settings.FinancialPeriods

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nate.autofinance.ui.components.AppTopBarPageTitle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialPeriodsScreen(
    periods: List<String>,
    selected: Set<Int>,
    onBack: () -> Unit = {},
    onSelectPeriod: (Int) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Períodos financeiros" + if (selected.isNotEmpty()) " selecionados: ${selected.size}" else "",
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            periods.forEachIndexed { index, label ->
                val isSelected = selected.contains(index)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPeriod(index) },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer
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
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                            )
                        }
                    }
                }
            }

            if (selected.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Excluir", color = Color.White)
                }
            }
        }
    }
}

@Preview
@Composable

fun PreviewFinancialPeriodsScreen() {
    FinancialPeriodsScreen(
        periods = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho"),
        selected = setOf(1, 3)
    )
}
