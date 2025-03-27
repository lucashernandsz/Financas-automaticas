package com.nate.autofinance.ui.screens.transactionList

import CategoryFilterRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.nate.autofinance.R
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import java.text.SimpleDateFormat
import java.util.*

// Exemplo de formatação de valor para “1.500,00”
fun Double.toBrazilianCurrency(): String {
    return String.format("%,.2f", this)
        .replace(',', 'X')
        .replace('.', ',')
        .replace('X', '.')
}

// Tela de lista de transações com NavigationBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onDashboardClick: () -> Unit = {},
    onTransactionsClick: () -> Unit = {}
) {
    // Lista de transações fictícias para demonstração
    val dummyTransactions = listOf(
        Transaction(id = 1, date = Date(), amount = 1500.0, description = "Salário", category = "Ganho"),
        Transaction(id = 2, date = Date(), amount = -45.90, description = "Almoço", category = "Despesa"),
        Transaction(id = 3, date = Date(), amount = -12.50, description = "Café", category = "Despesa"),
        Transaction(id = 4, date = Date(), amount = 1500.0, description = "Apple Store", category = "Despesa")
    )

    // Estado local para a categoria selecionada
    var selectedCategory by remember { mutableStateOf("Todas") }

    // Filtra as transações de acordo com a categoria selecionada
    val filteredTransactions = when (selectedCategory) {
        "Todas" -> dummyTransactions
        "Ganhos" -> dummyTransactions.filter { it.amount >= 0 }
        "Gastos" -> dummyTransactions.filter { it.amount < 0 }
        "Importados" -> dummyTransactions // Ajuste a lógica se houver outra forma de filtrar
        else -> dummyTransactions
    }

    // Calcula o total com base nas transações filtradas
    val total = filteredTransactions.sumOf { it.amount }

    Scaffold(
        topBar = {
            AppTopBarPageTitle(stringResource(id = R.string.transaction_page_title), showSettingsButton = true)
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Transações") },
                    label = { Text("Transações") },
                    selected = true,
                    onClick = onTransactionsClick
                )
                /*NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = false,
                    onClick = onDashboardClick
                )*/

            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // ----------------------------
            // 1) Filtros de Categorias
            // ----------------------------
            CategoryFilterRow(
                categories = listOf("Todas", "Ganhos", "Gastos", "Importados"),
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ----------------------------
            // 2) Lista de Transações
            // ----------------------------
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // preenche o espaço vertical disponível
            ) {
                items(filteredTransactions) { transaction ->
                    TransactionItemCard(transaction = transaction)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ----------------------------
            // 3) Total + Botão Inserir
            // ----------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exemplo: "Total: -1500" ou "Total: 1.500,00"
                Text(
                    text = "Total: ${total.toBrazilianCurrency()}",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = {
                        // Lógica para inserir nova transação
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(text = "+ Inserir")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionListScreenPreview() {
    MaterialTheme {
        TransactionListScreen()
    }
}
