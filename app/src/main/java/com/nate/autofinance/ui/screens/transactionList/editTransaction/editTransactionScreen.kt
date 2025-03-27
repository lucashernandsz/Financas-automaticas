package com.nate.autofinance.ui.screens.transactionList.editTransaction
import CategoryFilterRow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.ui.components.AppTextField
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transaction: Transaction,
    onBack: () -> Unit = {},
    onSave: (Transaction) -> Unit = {}
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var dateText by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(transaction.date)) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var description by remember { mutableStateOf(transaction.description) }

    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Editar Transação",
                showBackButton = true,
                onBackButtonClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Categoria", style = MaterialTheme.typography.labelLarge)
            CategoryFilterRow(
                categories = listOf("Ganho", "Despesa", "Outros"),
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            AppTextField(
                value = amount,
                onValueChange = { amount = it },
                placeholder = "Valor",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            AppTextField(
                value = dateText,
                onValueChange = { dateText = it },
                placeholder = "Data (dd/MM/yyyy)"
            )
            AppTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Descrição",
                singleLine = false
            )
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val date = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateText)!!
                    } catch (_: Exception) { Date() }
                    onSave(
                        transaction.copy(
                            date = date,
                            amount = amountValue,
                            category = selectedCategory,
                            description = description
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Salvar", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditTransactionScreenPreview() {
    EditTransactionScreen(
        transaction = Transaction(
            id = 42,
            date = Date(),
            amount = 123.45,
            description = "Exemplo",
            category = "Despesa"
        )
    )
}
