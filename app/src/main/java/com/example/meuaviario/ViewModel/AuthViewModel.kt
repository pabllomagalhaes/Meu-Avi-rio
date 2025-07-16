package com.example.meuaviario

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

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
                    // Registro bem-sucedido
                    onSuccess()
                } else {
                    // Se o registro falhar, mostre a mensagem de erro
                    onError(task.exception?.message ?: "Ocorreu um erro desconhecido.")
                }
            }
    }

    fun loginUsuario(
        email: String,
        senha: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || senha.isBlank()) {
            onError("Email e senha não podem estar em branco.")
            return
        }

        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login bem-sucedido
                    onSuccess()
                } else {
                    // Se o login falhar, mostre a mensagem de erro
                    onError(task.exception?.message ?: "Email ou senha incorretos.")
                }
            }
    }
}