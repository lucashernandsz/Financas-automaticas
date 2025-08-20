package com.nate.autofinance.ui.screens.transactionList

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.ui.components.AppTextField
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import com.nate.autofinance.utils.Categories
import com.nate.autofinance.ui.screens.transactionList.addTransaction.AddTransactionState
import com.nate.autofinance.ui.screens.transactionList.addTransaction.AddTransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = viewModel(),            // injeta o VM
    initialCategory: String = Categories.INCOME, // categoria inicial
    onBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}                               // callback após salvar
) {
    val state by viewModel.state.collectAsState()

    // Campos de input
    var amount by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var description by remember { mutableStateOf("") }

    // preenche a data atual
    LaunchedEffect(Unit) {
        if (dateText.isEmpty()) {
            dateText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date())
        }
    }

    // quando a transação for salva com sucesso, volta e limpa estado
    LaunchedEffect(state) {
        if (state is AddTransactionState.Success) {
            onSaveSuccess()
            viewModel.resetState() // opcional, se quiser reutilizar a tela
        }
    }

    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Adicionar Transação",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Categoria", style = MaterialTheme.typography.labelLarge)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Categories.fixedCategories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            labelColor     = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            AppTextField(
                value = amount,
                onValueChange = { amount = it },
                placeholder    = "Valor",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            AppTextField(
                value = dateText,
                onValueChange = { dateText = it },
                placeholder    = "Data (dd/MM/yyyy)"
            )
            AppTextField(
                value      = description,
                onValueChange = { description = it },
                placeholder    = "Descrição",
                singleLine     = false
            )

            when (state) {
                is AddTransactionState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                }
                is AddTransactionState.Error   -> {
                    Text(
                        text  = (state as AddTransactionState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> { /* Idle ou Success já tratado acima */ }
            }

            Button(
                onClick = {
                    // monta o objeto sem IDs; o use case ajusta o resto
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val dateParsed = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateText)!!
                    } catch (_: Exception) {
                        Date()
                    }
                    viewModel.addTransaction(
                        Transaction(
                            date              = dateParsed,
                            amount            = amountValue,
                            description       = description,
                            category          = selectedCategory,
                            financialPeriodId = 0 // o use case vai buscar o período ativo
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled  = state !is AddTransactionState.Loading
            ) {
                Text("Salvar")
            }
        }
    }
}
