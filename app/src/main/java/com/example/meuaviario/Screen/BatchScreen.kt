package com.example.meuaviario

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchScreen(
    navController: NavController,
    viewModel: BatchViewModel = viewModel()
) {
    val batches = viewModel.batches.value
    var showDialog by remember { mutableStateOf(false) }
    var batchToEdit by remember { mutableStateOf<Batch?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var batchToDelete by remember { mutableStateOf<Batch?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Lotes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                batchToEdit = null // Garante que é um novo lote
                showDialog = true
            }) {
                Icon(Icons.Default.Add, "Adicionar Lote")
            }
        },
        content = { paddingValues ->
            if (batches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum lote registado.\nClique no botão '+' para adicionar o seu primeiro.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Aumenta o espaçamento
                ) {
                    items(batches) { batch ->
                        BatchItem(
                            batch = batch,
                            onEditClick = {
                                batchToEdit = it
                                showDialog = true
                            },
                            onDeleteClick = {
                                batchToDelete = it
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }

            if (showDialog) {
                AddBatchDialog(
                    batchToEdit = batchToEdit,
                    onDismiss = { showDialog = false },
                    onConfirm = { name, breed, numberOfHens ->
                        if (batchToEdit == null) { // Adicionar novo
                            viewModel.addBatch(name, breed, numberOfHens,
                                onSuccess = { showDialog = false },
                                onError = { /* Tratar erro */ }
                            )
                        } else { // Atualizar existente
                            viewModel.updateBatch(batchToEdit!!.id, name, breed, numberOfHens,
                                onSuccess = { showDialog = false },
                                onError = { /* Tratar erro */ }
                            )
                        }
                    }
                )
            }

            if (showDeleteConfirmDialog) {
                DeleteConfirmDialog(
                    onDismiss = { showDeleteConfirmDialog = false },
                    onConfirm = {
                        batchToDelete?.let {
                            viewModel.deleteBatch(it.id,
                                onSuccess = { showDeleteConfirmDialog = false },
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
fun BatchItem(batch: Batch, onEditClick: (Batch) -> Unit, onDeleteClick: (Batch) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = batch.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Raça: ${batch.breed}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Nº de Aves: ${batch.numberOfHens}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                batch.startDate?.let {
                    Text(
                        text = "Início: ${dateFormat.format(it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = { onEditClick(batch) }) {
                Icon(Icons.Default.Edit, "Editar Lote")
            }
            IconButton(onClick = { onDeleteClick(batch) }) {
                Icon(Icons.Default.Delete, "Eliminar Lote")
            }
        }
    }
}

@Composable
fun AddBatchDialog(
    batchToEdit: Batch?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(batchToEdit?.name ?: "") }
    var breed by remember { mutableStateOf(batchToEdit?.breed ?: "") }
    var numberOfHens by remember { mutableStateOf(batchToEdit?.numberOfHens?.toString() ?: "") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (batchToEdit == null) "Adicionar Novo Lote" else "Editar Lote") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do Lote") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Raça") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = numberOfHens,
                    onValueChange = { numberOfHens = it.filter { c -> c.isDigit() } },
                    label = { Text("Nº de Aves") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hensInt = numberOfHens.toIntOrNull()
                    if (name.isNotBlank() && breed.isNotBlank() && hensInt != null) {
                        onConfirm(name, breed, hensInt)
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
fun DeleteConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminação") },
        text = { Text("Tem a certeza de que deseja eliminar este lote? Esta ação não pode ser desfeita.") },
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
