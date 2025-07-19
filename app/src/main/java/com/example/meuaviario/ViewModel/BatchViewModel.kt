package com.example.meuaviario

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.meuaviario.data.Batch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import java.util.Date

class BatchViewModel : ViewModel() {

    // Estado para guardar a lista de lotes
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
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Utilizador não autenticado.")
            return
        }

        val batch = Batch(
            name = name,
            breed = breed,
            numberOfHens = numberOfHens,
            startDate = Date() // A data é definida no momento da criação
        )

        firestore.collection("users").document(userId)
            .collection("batches")
            .add(batch)
            .addOnSuccessListener {
                Log.d("BatchViewModel", "Lote adicionado com sucesso.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("BatchViewModel", "Erro ao adicionar lote.", e)
                onError(e.message ?: "Erro desconhecido.")
            }
    }
}
