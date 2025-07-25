package com.example.meuaviario

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    var isAuthenticated by mutableStateOf<Boolean?>(null)
        private set

    // --- FUNÇÃO EM FALTA ADICIONADA ---
    fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isAuthenticated = task.isSuccessful
            }
    }

    fun resetAuthState() {
        isAuthenticated = null
    }

    // Funções de login e registo com email/senha podem ser removidas se não forem mais necessárias
    fun loginUsuario(email: String, senha: String) {
        if (email.isBlank() || senha.isBlank()) {
            isAuthenticated = false
            return
        }
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                isAuthenticated = task.isSuccessful
            }
    }

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
}
