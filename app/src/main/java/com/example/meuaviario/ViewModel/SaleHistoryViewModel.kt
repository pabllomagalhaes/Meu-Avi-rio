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
            .orderBy("timestamp", Query.Direction.DESCENDING)
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

    // --- NOVA FUNÇÃO DE ATUALIZAÇÃO ---
    fun updateSale(
        saleId: String,
        quantityInDozens: Int,
        pricePerDozen: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Utilizador não autenticado.")
        val totalAmount = quantityInDozens * pricePerDozen
        val updatedData = mapOf(
            "quantityInDozens" to quantityInDozens,
            "pricePerDozen" to pricePerDozen,
            "totalAmount" to totalAmount
        )
        firestore.collection("users").document(userId).collection("sales").document(saleId)
            .update(updatedData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erro desconhecido.") }
    }

    // --- NOVA FUNÇÃO DE ELIMINAÇÃO ---
    fun deleteSale(saleId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onError("Utilizador não autenticado.")
        firestore.collection("users").document(userId).collection("sales").document(saleId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erro desconhecido.") }
    }
}
