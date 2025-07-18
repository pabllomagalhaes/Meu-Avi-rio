package com.example.meuaviario

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel : ViewModel() {

    var summary by mutableStateOf<AviarySummary?>(null)
        private set

    var weeklyProduction by mutableStateOf<List<Int>>(emptyList())
        private set

    // Novo estado para a Conversão Alimentar
    var feedConversionRatio by mutableStateOf<Double?>(null)
        private set

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    init {
        listenForSummaryUpdates()
        fetchWeeklyData() // Renomeado para buscar todos os dados semanais
    }

    private fun listenForSummaryUpdates() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("aviary").document("summary")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeViewModel", "Falha na escuta do summary.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    summary = snapshot.toObject<AviarySummary>()
                } else {
                    summary = AviarySummary()
                }
            }
    }

    // Função atualizada para buscar ovos E ração e calcular a conversão
    private fun fetchWeeklyData() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("daily_production")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(7)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dailyData = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject<DailyProduction>()
                }

                // Atualiza o gráfico
                weeklyProduction = dailyData.map { it.eggs }.reversed()

                // Calcula a conversão alimentar
                val totalEggs = dailyData.sumOf { it.eggs }
                val totalFeed = dailyData.sumOf { it.feedConsumed }

                if (totalEggs > 0) {
                    val dozens = totalEggs / 12.0
                    feedConversionRatio = totalFeed / dozens
                } else {
                    feedConversionRatio = 0.0
                }

                Log.d("HomeViewModel", "Dados semanais carregados. Ovos: $totalEggs, Ração: $totalFeed, Conversão: $feedConversionRatio")
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Falha ao buscar dados semanais.", e)
            }
    }

    fun updateEggsToday(eggCount: Int) {
        val userId = auth.currentUser?.uid ?: return

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val summaryRef = firestore.collection("users").document(userId).collection("aviary").document("summary")
        val dailyRecordRef = firestore.collection("users").document(userId).collection("daily_production").document(today)

        val dailyRecordUpdate = mapOf("eggs" to eggCount, "timestamp" to Date())
        val summaryUpdate = mapOf("eggsToday" to eggCount)

        firestore.batch().apply {
            set(summaryRef, summaryUpdate, SetOptions.merge())
            set(dailyRecordRef, dailyRecordUpdate, SetOptions.merge())
        }.commit()
            .addOnSuccessListener {
                Log.d("HomeViewModel", "Lote de escrita (ovos) bem-sucedido.")
                fetchWeeklyData() // Recarrega todos os dados
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Falha na escrita em lote de ovos.", e)
            }
    }

    fun updateActiveHens(henCount: Int) {
        val userId = auth.currentUser?.uid ?: return
        val dataToUpdate = mapOf("activeHens" to henCount)
        firestore.collection("users").document(userId)
            .collection("aviary").document("summary")
            .set(dataToUpdate, SetOptions.merge())
    }

    fun updateFeedConsumption(feedAmount: Double) {
        val userId = auth.currentUser?.uid ?: return

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val summaryRef = firestore.collection("users").document(userId).collection("aviary").document("summary")
        val dailyRecordRef = firestore.collection("users").document(userId).collection("daily_production").document(today)

        val summaryUpdate = mapOf("feedConsumedToday" to feedAmount)
        val dailyRecordUpdate = mapOf("feedConsumed" to feedAmount, "timestamp" to Date())

        firestore.batch().apply {
            set(summaryRef, summaryUpdate, SetOptions.merge())
            set(dailyRecordRef, dailyRecordUpdate, SetOptions.merge())
        }.commit()
            .addOnSuccessListener {
                Log.d("HomeViewModel", "Consumo de ração atualizado com sucesso.")
                fetchWeeklyData() // Recarrega todos os dados
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Falha ao atualizar consumo de ração.", e)
            }
    }
}
