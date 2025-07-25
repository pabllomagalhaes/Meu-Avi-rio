package com.example.meuaviario

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase

class ExpenseHistoryViewModel : ViewModel() {

    val expenses = mutableStateOf<List<Expense>>(emptyList())

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    init {
        fetchExpenses()
    }

    private fun fetchExpenses() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("expenses")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ExpenseHistoryVM", "Erro ao buscar despesas.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    expenses.value = snapshot.toObjects<Expense>()
                }
            }
    }

    // --- NOVA FUNÇÃO DE ATUALIZAÇÃO ---
    fun updateExpense(
        expenseId: String,
        description: String,
        amount: Double,
        category: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Utilizador não autenticado.")
        val updatedData = mapOf(
            "description" to description,
            "amount" to amount,
            "category" to category
        )
        firestore.collection("users").document(userId).collection("expenses").document(expenseId)
            .update(updatedData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erro desconhecido.") }
    }

    // --- NOVA FUNÇÃO DE ELIMINAÇÃO ---
    fun deleteExpense(expenseId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onError("Utilizador não autenticado.")
        firestore.collection("users").document(userId).collection("expenses").document(expenseId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erro desconhecido.") }
    }
}
