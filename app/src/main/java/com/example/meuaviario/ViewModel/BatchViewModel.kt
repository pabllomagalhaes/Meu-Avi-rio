package com.example.meuaviario

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import java.util.Date

class BatchViewModel : ViewModel() {

    val batches = mutableStateOf<List<Batch>>(emptyList())

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    init {
        fetchBatches()
    }

    private fun fetchBatches() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("batches")
            .orderBy("startDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BatchViewModel", "Erro ao buscar lotes.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    batches.value = snapshot.toObjects<Batch>()
                }
            }
    }

    fun addBatch(
        name: String,
        breed: String,
        numberOfHens: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Utilizador não autenticado.")
        val batch = Batch(name = name, breed = breed, numberOfHens = numberOfHens, startDate = Date())
        firestore.collection("users").document(userId).collection("batches").add(batch)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erro desconhecido.") }
    }

    // --- NOVA FUNÇÃO DE ATUALIZAÇÃO ---
    fun updateBatch(
        batchId: String,
        name: String,
        breed: String,
        numberOfHens: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Utilizador não autenticado.")
        val updatedData = mapOf(
            "name" to name,
            "breed" to breed,
            "numberOfHens" to numberOfHens
        )
        firestore.collection("users").document(userId).collection("batches").document(batchId)
            .update(updatedData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erro desconhecido.") }
    }

    // --- NOVA FUNÇÃO DE ELIMINAÇÃO ---
    fun deleteBatch(batchId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onError("Utilizador não autenticado.")
        firestore.collection("users").document(userId).collection("batches").document(batchId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erro desconhecido.") }
    }
}
