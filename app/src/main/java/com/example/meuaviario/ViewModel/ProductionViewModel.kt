package com.example.meuaviario

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductionViewModel : ViewModel() {

    val dailyRecords = mutableStateOf<List<DailyProduction>>(emptyList())

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    init {
        fetchDailyRecords()
    }

    private fun fetchDailyRecords() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("daily_production")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProductionVM", "Erro ao buscar registos.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    dailyRecords.value = snapshot.toObjects<DailyProduction>()
                }
            }
    }

    fun saveDailyRecord(
        recordId: String?,
        eggCount: Int,
        feedAmount: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Utilizador não autenticado.")
        if (recordId != null) {
            val updateData = mapOf("eggs" to eggCount, "feedConsumed" to feedAmount)
            firestore.collection("users").document(userId).collection("daily_production").document(recordId)
                .update(updateData)
                .addOnSuccessListener {
                    if (isToday(recordId)) {
                        updateSummary(userId, eggCount, feedAmount)
                    }
                    onSuccess()
                }
                .addOnFailureListener { e -> onError(e.message ?: "Erro desconhecido.") }
        } else {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val dailyRecordRef = firestore.collection("users").document(userId).collection("daily_production").document(today)
            val dailyRecord = DailyProduction(eggs = eggCount, feedConsumed = feedAmount)
            dailyRecordRef.set(dailyRecord, SetOptions.merge())
                .addOnSuccessListener {
                    updateSummary(userId, eggCount, feedAmount)
                    onSuccess()
                }
                .addOnFailureListener { e -> onError(e.message ?: "Erro desconhecido.") }
        }
    }

    // --- NOVA FUNÇÃO DE ELIMINAÇÃO ---
    fun deleteProductionRecord(recordId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onError("Utilizador não autenticado.")
        firestore.collection("users").document(userId).collection("daily_production").document(recordId)
            .delete()
            .addOnSuccessListener {
                Log.d("ProductionViewModel", "Registo eliminado com sucesso.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ProductionViewModel", "Erro ao eliminar registo.", e)
                onError(e.message ?: "Erro desconhecido.")
            }
    }

    private fun isToday(docId: String): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return docId == today
    }

    private fun updateSummary(userId: String, eggCount: Int, feedAmount: Double) {
        val summaryRef = firestore.collection("users").document(userId).collection("aviary").document("summary")
        val summaryUpdate = mapOf(
            "eggsToday" to eggCount,
            "feedConsumedToday" to feedAmount
        )
        summaryRef.set(summaryUpdate, SetOptions.merge())
    }
}
