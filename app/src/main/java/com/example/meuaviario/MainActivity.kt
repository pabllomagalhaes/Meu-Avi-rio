package com.example.meuaviario

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meuaviario.ui.theme.MeuAviarioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeuAviarioTheme {
                // O NavController é o cérebro da navegação
                val navController = rememberNavController()

                // NavHost é o container que exibirá a tela correta
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        // Passamos o navController para a LoginScreen
                        LoginScreen(navController = navController)
                    }
                    composable("home") {
                        HomeScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(authViewModel.isAuthenticated) {
        if (authViewModel.isAuthenticated == true) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
            authViewModel.resetAuthState()
        } else if (authViewModel.isAuthenticated == false) {
            Toast.makeText(context, "Email ou senha incorretos.", Toast.LENGTH_LONG).show()
            authViewModel.resetAuthState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Meu Aviário Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = senha, onValueChange = { senha = it }, label = { Text("Senha") })
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            authViewModel.loginUsuario(email, senha)
        }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            authViewModel.registrarUsuario(
                email = email,
                senha = senha,
                onSuccess = {
                    Toast.makeText(context, "Registro bem-sucedido! Faça o login.", Toast.LENGTH_SHORT).show()
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
        val navController = rememberNavController()
        LoginScreen(navController = navController)
    }
}
