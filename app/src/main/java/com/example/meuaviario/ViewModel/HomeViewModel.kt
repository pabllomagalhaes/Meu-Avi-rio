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
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeViewModel : ViewModel() {

    var summary by mutableStateOf<AviarySummary?>(null)
        private set
    // Alterado para conter a lista completa de dados diários
    var weeklyProductionData by mutableStateOf<List<DailyProduction>>(emptyList())
        private set
    var feedConversionRatio by mutableStateOf<Double?>(null)
        private set
    var monthlyExpenses by mutableStateOf<Double?>(null)
        private set
    var monthlySales by mutableStateOf<Double?>(null)
        private set
    var alerts by mutableStateOf<List<String>>(emptyList())
        private set

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    init {
        listenForSummaryUpdates()
        fetchWeeklyData()
        listenForMonthlyExpenses()
        listenForMonthlySales()
        listenForBatchUpdatesAndSyncHenCount()
    }

    private fun listenForBatchUpdatesAndSyncHenCount() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("batches")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val totalHens = snapshot.documents.sumOf { it.getLong("numberOfHens")?.toInt() ?: 0 }
                    updateActiveHens(totalHens)
                }
            }
    }

    private fun listenForSummaryUpdates() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("aviary").document("summary")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    summary = snapshot.toObject<AviarySummary>()
                }
            }
    }

    private fun fetchWeeklyData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("daily_production")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(7)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dailyData = querySnapshot.documents.mapNotNull { it.toObject<DailyProduction>() }
                // Guarda a lista completa e invertida
                weeklyProductionData = dailyData.reversed()

                val totalEggs = dailyData.sumOf { it.eggs }
                val totalFeed = dailyData.sumOf { it.feedConsumed }

                if (totalEggs > 0) {
                    val dozens = totalEggs / 12.0
                    feedConversionRatio = totalFeed / dozens
                } else {
                    feedConversionRatio = 0.0
                }

                checkForProductionDrop(dailyData.map { it.eggs })
            }
    }

    private fun checkForProductionDrop(dailyEggCounts: List<Int>) {
        val newAlerts = mutableListOf<String>()
        if (dailyEggCounts.size >= 3) {
            val averageOfFirstDays = dailyEggCounts.dropLast(1).average()
            val lastDayProduction = dailyEggCounts.last().toDouble()

            if (lastDayProduction < averageOfFirstDays * 0.85) {
                newAlerts.add("Queda na produção. Verifique o bem-estar das aves.")
            }
        }
        alerts = newAlerts
    }

    private fun listenForMonthlyExpenses() {
        val userId = auth.currentUser?.uid ?: return
        val (startOfMonth, startOfNextMonth) = getMonthDateRange()
        firestore.collection("users").document(userId)
            .collection("expenses")
            .whereGreaterThanOrEqualTo("timestamp", startOfMonth)
            .whereLessThan("timestamp", startOfNextMonth)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    monthlyExpenses = snapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                }
            }
    }

    private fun listenForMonthlySales() {
        val userId = auth.currentUser?.uid ?: return
        val (startOfMonth, startOfNextMonth) = getMonthDateRange()
        firestore.collection("users").document(userId)
            .collection("sales")
            .whereGreaterThanOrEqualTo("timestamp", startOfMonth)
            .whereLessThan("timestamp", startOfNextMonth)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    monthlySales = snapshot.documents.sumOf { it.getDouble("totalAmount") ?: 0.0 }
                }
            }
    }

    private fun getMonthDateRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1); calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
        val startOfMonth = calendar.time
        calendar.add(Calendar.MONTH, 1)
        val startOfNextMonth = calendar.time
        return Pair(startOfMonth, startOfNextMonth)
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
        }.commit().addOnSuccessListener { fetchWeeklyData() }
    }

    private fun updateActiveHens(henCount: Int) {
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
        }.commit().addOnSuccessListener { fetchWeeklyData() }
    }
}
