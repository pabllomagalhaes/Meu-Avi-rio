package com.example.meuaviario

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    // Variável que a UI vai observar. Inicia como nula.
    var isAuthenticated by mutableStateOf<Boolean?>(null)
        private set

    fun registrarUsuario(
        email: String,
        senha: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || senha.isBlank()) {
            onError("Email e senha não podem estar em branco.")
            return
        }

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Ocorreu um erro desconhecido.")
                }
            }
    }

    fun loginUsuario(email: String, senha: String) {
        if (email.isBlank() || senha.isBlank()) {
            // Podemos tratar o erro de forma mais robusta depois
            return
        }

        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                // Apenas atualizamos o estado. A UI vai reagir a isso.
                isAuthenticated = task.isSuccessful
            }
    }

    // Função para resetar o estado após a navegação
    fun resetAuthState() {
        isAuthenticated = null
    }
}