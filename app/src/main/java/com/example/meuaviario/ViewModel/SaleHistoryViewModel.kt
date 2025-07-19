package com.example.meuaviario

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase

class SaleHistoryViewModel : ViewModel() {

    // Estado para guardar a lista de vendas
    val sales = mutableStateOf<List<Sale>>(emptyList())

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    init {
        fetchSales()
    }

    private fun fetchSales() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("sales")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Mais recentes primeiro
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SaleHistoryVM", "Erro ao buscar vendas.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    sales.value = snapshot.toObjects<Sale>()
                }
            }
    }
}
