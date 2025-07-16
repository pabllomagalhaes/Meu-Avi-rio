package com.example.meuaviario // Verifique se este é o seu package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meuaviario.ui.theme.MeuAviarioTheme
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeuAviarioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@Composable
fun LoginScreen(authViewModel: AuthViewModel = viewModel()) {
    // Esta variável guarda o que o usuário digita. São a "memória" da tela.
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val context = LocalContext.current // <- Precisamos do contexto para mostrar a mensagem

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Meu Aviário", style = MaterialTheme.typography.headlineMedium)
        Text("Faça o login ou cadastre-se", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { authViewModel.loginUsuario(
            email = email,
            senha = senha,
            onSuccess = {
                Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
            },
            onError = { mensagemDeErro ->
                Toast.makeText(context, "Erro: $mensagemDeErro", Toast.LENGTH_LONG).show()
            }
        ) }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AJUSTE NO BOTÃO REGISTRAR
        Button(onClick = {
            authViewModel.registrarUsuario(
                email = email,
                senha = senha,
                onSuccess = {
                    Toast.makeText(context, "Registro bem-sucedido!", Toast.LENGTH_SHORT).show()
                },
                onError = { mensagemDeErro ->
                    Toast.makeText(context, "Erro: $mensagemDeErro", Toast.LENGTH_LONG).show()
                }
            )
        }) {
            Text("Registrar")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MeuAviarioTheme {
        LoginScreen()
    }
}