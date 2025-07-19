package com.example.meuaviario

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SaleViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    fun saveSale(
        quantityInDozens: Int,
        pricePerDozen: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Utilizador não autenticado.")
            return
        }

        val totalAmount = quantityInDozens * pricePerDozen
        val sale = Sale(
            quantityInDozens = quantityInDozens,
            pricePerDozen = pricePerDozen,
            totalAmount = totalAmount
        )

        firestore.collection("users").document(userId)
            .collection("sales")
            .add(sale)
            .addOnSuccessListener {
                Log.d("SaleViewModel", "Venda guardada com sucesso.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("SaleViewModel", "Erro ao guardar venda.", e)
                onError(e.message ?: "Erro desconhecido.")
            }
    }
}
