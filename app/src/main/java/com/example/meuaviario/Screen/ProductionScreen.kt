package com.example.meuaviario

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionScreen(
    navController: NavController,
    viewModel: ProductionViewModel = viewModel()
) {
    val dailyRecords = viewModel.dailyRecords.value
    var showDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<DailyProduction?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<DailyProduction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Histórico de Produção") })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                    icon = { Icon(Icons.Outlined.Analytics, contentDescription = "Painel") },
                    label = { Text("Painel") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Já estamos aqui */ },
                    icon = { Icon(Icons.Outlined.EditNote, contentDescription = "Produção") },
                    label = { Text("Produção") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("batch") { popUpTo("home") { inclusive = true } } },
                    icon = { Icon(Icons.Outlined.ContentPaste, contentDescription = "Lotes") },
                    label = { Text("Lotes") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("expense_history") { popUpTo("home") { inclusive = true } } },
                    icon = { Icon(Icons.Outlined.Store, contentDescription = "Despesas") },
                    label = { Text("Despesas") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("sale_history") { popUpTo("home") { inclusive = true } } },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Vendas") },
                    label = { Text("Vendas") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                recordToEdit = null // Garante que é um novo registo
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Registo")
            }
        },
        content = { paddingValues ->
            if (dailyRecords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum registo de produção encontrado.\nClique no botão '+' para adicionar o de hoje.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dailyRecords) { record ->
                        ProductionItem(
                            record = record,
                            onEditClick = {
                                recordToEdit = it
                                showDialog = true
                            },
                            onDeleteClick = {
                                recordToDelete = it
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            if (showDialog) {
                AddOrEditProductionDialog(
                    recordToEdit = recordToEdit,
                    onDismiss = { showDialog = false },
                    onConfirm = { eggCount, feedAmount ->
                        viewModel.saveDailyRecord(
                            recordId = recordToEdit?.id,
                            eggCount = eggCount,
                            feedAmount = feedAmount,
                            onSuccess = { showDialog = false },
                            onError = { /* Tratar erro */ }
                        )
                    }
                )
            }

            if (showDeleteDialog) {
                DeleteProductionConfirmDialog(
                    onDismiss = { showDeleteDialog = false },
                    onConfirm = {
                        recordToDelete?.let {
                            viewModel.deleteProductionRecord(it.id,
                                onSuccess = { showDeleteDialog = false },
                                onError = { /* Tratar erro */ }
                            )
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun ProductionItem(
    record: DailyProduction,
    onEditClick: (DailyProduction) -> Unit,
    onDeleteClick: (DailyProduction) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.timestamp?.let { dateFormat.format(it) } ?: "Data indefinida",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ovos Coletados: ${record.eggs}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Ração Consumida: ${record.feedConsumed} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onEditClick(record) }) {
                Icon(Icons.Default.Edit, "Editar Registo")
            }
            IconButton(onClick = { onDeleteClick(record) }) {
                Icon(Icons.Default.Delete, "Eliminar Registo")
            }
        }
    }
}

@Composable
fun AddOrEditProductionDialog(
    recordToEdit: DailyProduction?,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double) -> Unit
) {
    var eggCount by remember { mutableStateOf(recordToEdit?.eggs?.toString() ?: "") }
    var feedAmount by remember { mutableStateOf(recordToEdit?.feedConsumed?.toString() ?: "") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (recordToEdit == null) "Adicionar Registo Diário" else "Editar Registo de ${recordToEdit?.timestamp?.let { SimpleDateFormat("dd/MM", Locale.getDefault()).format(it) }}") },
        text = {
            Column {
                OutlinedTextField(
                    value = eggCount,
                    onValueChange = { eggCount = it.filter { c -> c.isDigit() } },
                    label = { Text("Ovos Coletados") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = feedAmount,
                    onValueChange = { feedAmount = it },
                    label = { Text("Ração Consumida (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val eggInt = eggCount.toIntOrNull()
                    val feedDouble = feedAmount.replace(',', '.').toDoubleOrNull()
                    if (eggInt != null && feedDouble != null) {
                        onConfirm(eggInt, feedDouble)
                    } else {
                        Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeleteProductionConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminação") },
        text = { Text("Tem a certeza de que deseja eliminar este registo de produção? Esta ação não pode ser desfeita.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
