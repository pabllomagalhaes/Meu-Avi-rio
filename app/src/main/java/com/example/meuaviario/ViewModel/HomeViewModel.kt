package com.example.meuaviario

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class HomeViewModel : ViewModel() {

    var summary by mutableStateOf<AviarySummary?>(null)
        private set

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    init {
        listenForSummaryUpdates()
    }

    private fun listenForSummaryUpdates() {
        val userId = auth.currentUser?.uid
        Log.d("HomeViewModel", "Tentando buscar dados para o usuário: $userId")

        if (userId == null) {
            Log.e("HomeViewModel", "Erro Crítico: User ID é nulo.")
            return
        }

        firestore.collection("users").document(userId)
            .collection("aviary").document("summary")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeViewModel", "Falha na escuta do Firestore.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("HomeViewModel", "Snapshot recebido com sucesso. Dados: ${snapshot.data}")
                    summary = snapshot.toObject<AviarySummary>()
                } else {
                    // Se o documento não existe, mostramos um resumo zerado.
                    Log.w("HomeViewModel", "Documento 'summary' não encontrado. Exibindo dados zerados.")
                    summary = AviarySummary()
                }
            }
    }

    fun updateEggsToday(eggCount: Int) {
        val userId = auth.currentUser?.uid ?: return

        // Usamos .set com merge para criar o documento se ele não existir, ou atualizá-lo se existir.
        val dataToUpdate = mapOf("eggsToday" to eggCount)
        firestore.collection("users").document(userId)
            .collection("aviary").document("summary")
            .set(dataToUpdate, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("HomeViewModel", "Contagem de ovos atualizada com sucesso.")
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Falha ao atualizar os ovos.", e)
            }
    }
    fun updateActiveHens(henCount: Int) {
        val userId = auth.currentUser?.uid ?: return
        val dataToUpdate = mapOf("activeHens" to henCount)
        firestore.collection("users").document(userId)
            .collection("aviary").document("summary")
            .set(dataToUpdate, SetOptions.merge())
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Falha ao atualizar as galinhas.", e)
            }
    }
}
