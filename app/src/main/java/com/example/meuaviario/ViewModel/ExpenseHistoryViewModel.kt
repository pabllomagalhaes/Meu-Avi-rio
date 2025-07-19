package com.example.meuaviario.ViewModel



import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.meuaviario.Expense
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase

class ExpenseHistoryViewModel : ViewModel() {

    // Estado para guardar a lista de despesas
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
            .orderBy("timestamp", Query.Direction.DESCENDING) // Mais recentes primeiro
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
}
