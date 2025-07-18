package com.example.meuaviario

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    var showEggDialog by remember { mutableStateOf(false) }
    var showHenDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meu Aviário") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showEggDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Coleta de Ovos")
            }
        },
        content = { paddingValues ->
            HomeContent(
                paddingValues = paddingValues,
                summary = homeViewModel.summary,
                onHenCardClick = { showHenDialog = true }
            )

            if (showEggDialog) {
                EggCollectionDialog(
                    onDismiss = { showEggDialog = false },
                    onConfirm = { count ->
                        homeViewModel.updateEggsToday(count)
                        showEggDialog = false
                    }
                )
            }

            if (showHenDialog) {
                HenCountDialog(
                    onDismiss = { showHenDialog = false },
                    onConfirm = { count ->
                        homeViewModel.updateActiveHens(count)
                        showHenDialog = false
                    }
                )
            }
        }
    )
}

@Composable
fun HenCountDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var henCountInput by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atualizar Galinhas Ativas") },
        text = {
            OutlinedTextField(
                value = henCountInput,
                onValueChange = { henCountInput = it.filter { char -> char.isDigit() } },
                label = { Text("Quantidade de galinhas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = henCountInput.toIntOrNull()
                    if (count != null) {
                        onConfirm(count)
                    } else {
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
fun HomeContent(
    paddingValues: PaddingValues,
    summary: AviarySummary?,
    onHenCardClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text("Painel de Controle", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (summary == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Text("Taxa de Postura:", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    val postureRate = if (summary.activeHens > 0) {
                        (summary.eggsToday.toDouble() / summary.activeHens) * 100
                    } else {
                        0.0
                    }
                    val formattedRate = DecimalFormat("0.0").format(postureRate)
                    Text("$formattedRate %", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable(onClick = onHenCardClick)
            ) {
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

            // --- NOVO CARD COM GRÁFICO ---
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Produção (Últimos 7 dias)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dados simulados para o gráfico
                    val weeklyProduction = listOf(42, 40, 38, 44, 45, 41, 43)

                    ProductionChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        data = weeklyProduction
                    )
                }
            }
        }
    }
}

// --- NOVO COMPONENT DE GRÁFICO ---
@Composable
fun ProductionChart(modifier: Modifier = Modifier, data: List<Int>) {
    val primaryColor = MaterialTheme.colorScheme.primary

    if (data.isEmpty()) return

    Canvas(modifier = modifier) {
        val maxValue = data.maxOrNull() ?: 0
        val minValue = data.minOrNull() ?: 0
        val valueRange = (maxValue - minValue).toFloat().coerceAtLeast(1f)

        val path = Path()

        data.forEachIndexed { index, value ->
            val x = size.width * (index.toFloat() / (data.size - 1))
            val y = size.height * (1 - (value - minValue) / valueRange)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            // Desenha um pequeno círculo em cada ponto de dado
            drawCircle(color = primaryColor, radius = 8f, center = Offset(x, y))
        }

        // Desenha a linha do gráfico
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 5f)
        )
    }
}
