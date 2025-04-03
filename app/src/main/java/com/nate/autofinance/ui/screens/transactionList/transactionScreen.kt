package com.nate.autofinance.ui.screens.transactionList

import CategoryFilterRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.nate.autofinance.R
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import java.util.*

// Exemplo de formatação de valor para “1.500,00”
fun Double.toBrazilianCurrency(): String {
    return String.format("%,.2f", this)
        .replace(',', 'X')
        .replace('.', ',')
        .replace('X', '.')
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onDashboardClick: () -> Unit = {},
    onTransactionsClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}, // novo callback para a engrenagem de settings
    onTransactionClick: (Transaction) -> Unit = {} // novo callback para edição de transação
) {
    // Lista de transações fictícias para demonstração
    val dummyTransactions = listOf(
        Transaction(id = 1, date = Date(), amount = 1500.0, description = "Salário", category = "Ganho"),
        Transaction(id = 2, date = Date(), amount = -45.90, description = "Almoço", category = "Despesa"),
        Transaction(id = 3, date = Date(), amount = -12.50, description = "Café", category = "Despesa"),
        Transaction(id = 4, date = Date(), amount = 1500.0, description = "Apple Store", category = "Despesa")
    )

    var selectedCategory by remember { mutableStateOf("Todas") }
    val filteredTransactions = when (selectedCategory) {
        "Todas" -> dummyTransactions
        "Ganhos" -> dummyTransactions.filter { it.amount >= 0 }
        "Gastos" -> dummyTransactions.filter { it.amount < 0 }
        "Importados" -> dummyTransactions // ajuste conforme a lógica de filtragem
        else -> dummyTransactions
    }
    val total = filteredTransactions.sumOf { it.amount }

    Scaffold(
        topBar = {
            // Supondo que AppTopBarPageTitle foi ajustado para receber onSettingsClick
            AppTopBarPageTitle(
                text = stringResource(id = R.string.transaction_page_title),
                showSettingsButton = true,
                onSettingsClick = onSettingsClick,
            )
        },
        /*bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Transações") },
                    label = { Text("Transações") },
                    selected = true,
                    onClick = onTransactionsClick
                )
                /* Outros itens do NavigationBar, se necessário */
            }
        }*/
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            CategoryFilterRow(
                categories = listOf("Todas", "Ganhos", "Gastos", "Importados"),
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredTransactions) { transaction ->
                    // Ao clicar em um item, dispara o callback para edição
                    TransactionItemCard(
                        transaction = transaction,
                        modifier = Modifier.clickable { onTransactionClick(transaction) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ${total.toBrazilianCurrency()}",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = onAddTransactionClick,
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
