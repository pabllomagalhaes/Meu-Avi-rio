package com.example.meuaviario

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meuaviario.ui.theme.MeuAviarioTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeuAviarioTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController = navController) }
                    composable("home") { HomeScreen(navController = navController) }
                    composable("expense") { ExpenseScreen(navController = navController) }
                    composable("expense_history") { ExpenseHistoryScreen(navController = navController) }
                    composable("sale") { SaleScreen(navController = navController) }
                    composable("batch") { BatchScreen(navController = navController) }
                    composable("sale_history") { SaleHistoryScreen(navController = navController) }
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
    val context = LocalContext.current

    // --- Configuração do Google Sign-In ---
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("78955678430-a2cs975eolqtqd06mqmd9tbqdv8kmnft.apps.googleusercontent.com") // Lembre-se de colar o seu Web Client ID aqui
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val idToken = account.idToken!!
            authViewModel.signInWithGoogle(idToken)
        } catch (e: ApiException) {
            Toast.makeText(context, "Falha no login com Google.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(authViewModel.isAuthenticated) {
        if (authViewModel.isAuthenticated == true) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
            authViewModel.resetAuthState()
        } else if (authViewModel.isAuthenticated == false) {
            Toast.makeText(context, "Falha na autenticação.", Toast.LENGTH_LONG).show()
            authViewModel.resetAuthState()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.imagemapp),
                contentDescription = "Logo Meu Aviário",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Bem-vindo!", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Acesse com sua conta Google para começar.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                // Pode adicionar um ícone do Google aqui no futuro
                Text("Entrar com o Google")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MeuAviarioTheme {
        LoginScreen(navController = rememberNavController())
    }
}
