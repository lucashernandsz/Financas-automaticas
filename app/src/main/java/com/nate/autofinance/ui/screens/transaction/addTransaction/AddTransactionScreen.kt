package com.nate.autofinance.ui.screens.transaction.addTransaction

import CategoryFilterRow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import com.nate.autofinance.viewmodel.AddTransactionState
import com.nate.autofinance.viewmodel.AddTransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = viewModel(),
    initialCategory: String = Categories.Income.name,
    onBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    var amount by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var description by remember { mutableStateOf("") }
    var isCredit by remember { mutableStateOf(false) }
    var selectedInstallments by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        if (dateText.isEmpty()) {
            dateText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }
    }

    LaunchedEffect(state) {
        if (state is AddTransactionState.Success) {
            onSaveSuccess()
            viewModel.resetState()
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
            CategoryFilterRow(
                categories = Categories.fixedCategories.map { it.name },
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Valor (ocupa o espaço flexível)
                Box(Modifier.weight(1f)) {
                    Column {
                        Text("Valor", style = MaterialTheme.typography.labelLarge)
                        AppTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            placeholder = "Ex: 50.00",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                // 2. Parcelas (só aparece quando for crédito) - MOVIDO PARA CÁ
                if (isCredit) {
                    var ddExpanded by remember { mutableStateOf(false) }
                    Column {
                        Text("Parcelas", style = MaterialTheme.typography.labelLarge)
                        ExposedDropdownMenuBox(
                            expanded = ddExpanded,
                            onExpandedChange = { ddExpanded = !ddExpanded }
                        ) {
                            TextField(
                                modifier = Modifier
                                    .menuAnchor()
                                    .widthIn(min = 96.dp),
                                value = "$selectedInstallments",
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = ddExpanded)
                                },
                                // ESTILO APLICADO AQUI
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = ddExpanded,
                                onDismissRequest = { ddExpanded = false }
                            ) {
                                (1..12).forEach { installment ->
                                    DropdownMenuItem(
                                        text = { Text("$installment") },
                                        onClick = {
                                            selectedInstallments = installment
                                            ddExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. É crédito? (checkbox compacto com label)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Crédito")
                    Spacer(Modifier.height(6.dp))
                    Checkbox(
                        checked = isCredit,
                        onCheckedChange = { isCredit = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Text("Data", style = MaterialTheme.typography.labelLarge)
            AppTextField(
                value = dateText,
                onValueChange = { dateText = it },
                placeholder = "(dd/MM/yyyy)"
            )
            Text("Descrição", style = MaterialTheme.typography.labelLarge)
            AppTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Ex: Compra no mercado",
                singleLine = false
            )

            when (val currentState = state) {
                is AddTransactionState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                }
                is AddTransactionState.Error -> {
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> { /* Idle ou Success já tratado acima */
                }
            }

            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val dateParsed = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateText)!!
                    } catch (_: Exception) {
                        Date()
                    }
                    viewModel.addTransaction(
                        Transaction(
                            date = dateParsed,
                            amount = amountValue,
                            description = description,
                            category = selectedCategory,
                            isCredit = isCredit,
                            numberOfInstallments = if (isCredit) selectedInstallments else 1,
                            financialPeriodId = 0
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is AddTransactionState.Loading
            ) {
                Text("Salvar")
            }
        }
    }
}