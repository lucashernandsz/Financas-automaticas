package com.nate.autofinance.ui.screens.transactionList

import CategoryFilterRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nate.autofinance.R
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import com.nate.autofinance.utils.Categories
import com.nate.autofinance.viewmodel.TransactionViewModel
import java.util.*

// Formata valor em estilo brasileiro, ex: "1.500,00"
fun Double.toBrazilianCurrency(): String =
    String.format("%,.2f", this)
        .replace(',', 'X')
        .replace('.', ',')
        .replace('X', '.')

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel,
    onDashboardClick: () -> Unit = {},
    onTransactionsClick: () -> Unit = {},
    onAddTransactionClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {}
) {
    // 1) Observa categorias, filtro e transações do ViewModel
    val categories by viewModel.categories.collectAsState()            // :contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1}
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()

    // 2) Carrega transações ao montar a tela
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()                                    // :contentReference[oaicite:2]{index=2}&#8203;:contentReference[oaicite:3]{index=3}
    }

    val total = transactions.sumOf { it.amount }

    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = stringResource(id = R.string.transaction_page_title),
                showSettingsButton = true,
                onSettingsClick = onSettingsClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 3) Filtro de categorias
            CategoryFilterRow(
                categories = listOf("All") + Categories.fixedCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = viewModel::setCategoryFilter
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4) Lista de transações
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(transactions) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTransactionClick(tx) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // 5) Total e botão de adicionar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total: ${total.toBrazilianCurrency()}",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = {onAddTransactionClick(selectedCategory)},
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(text = "+ Inserir")
                }
            }
        }
    }
}
