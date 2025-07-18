package com.example.meuaviario

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ExpenseViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    fun saveExpense(
        description: String,
        amount: Double,
        category: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Usuário não autenticado.")
            return
        }

        val expense = Expense(
            description = description,
            amount = amount,
            category = category
        )

        // Cria um novo documento na sub-coleção 'expenses'
        firestore.collection("users").document(userId)
            .collection("expenses")
            .add(expense)
            .addOnSuccessListener {
                Log.d("ExpenseViewModel", "Despesa salva com sucesso.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ExpenseViewModel", "Erro ao salvar despesa.", e)
                onError(e.message ?: "Erro desconhecido.")
            }
    }
}
