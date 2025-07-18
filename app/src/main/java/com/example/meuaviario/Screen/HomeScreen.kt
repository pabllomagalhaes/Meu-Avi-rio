package com.example.meuaviario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meuaviario.AviarySummary // Adicionado import para resolver o erro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    // Variável para controlar a visibilidade do pop-up
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meu Aviário") })
        },
        // Adicionando o botão flutuante
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Coleta")
            }
        },
        content = { paddingValues ->
            HomeContent(
                paddingValues = paddingValues,
                summary = homeViewModel.summary
            )

            // Se showDialog for true, o pop-up será exibido
            if (showDialog) {
                EggCollectionDialog(
                    onDismiss = { showDialog = false },
                    onConfirm = { count ->
                        homeViewModel.updateEggsToday(count)
                        showDialog = false
                    }
                )
            }
        }
    )
}

@Composable
fun EggCollectionDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var eggCountInput by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Coleta de Ovos") },
        text = {
            OutlinedTextField(
                value = eggCountInput,
                onValueChange = { eggCountInput = it.filter { char -> char.isDigit() } },
                label = { Text("Quantidade de ovos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = eggCountInput.toIntOrNull()
                    if (count != null) {
                        onConfirm(count)
                    } else {
                        // Opcional: Mostrar um aviso se o campo estiver vazio
                        android.widget.Toast.makeText(context, "Por favor, insira um número.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Confirmar")
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
fun HomeContent(paddingValues: PaddingValues, summary: AviarySummary?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text("Resumo do Aviário", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (summary == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Text("Galinhas Ativas:", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${summary.activeHens}", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Text("Ovos Coletados Hoje:", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${summary.eggsToday}", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Text("Produção Total (Últimos 7 dias):", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${summary.totalEggsLast7Days}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
