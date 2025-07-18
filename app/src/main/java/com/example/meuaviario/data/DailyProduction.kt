package com.example.meuaviario

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DailyProduction(
    val eggs: Int = 0,
    @ServerTimestamp val timestamp: Date? = null,
    val feedConsumed: Double = 0.0 // <-- Esta linha é a correção
)