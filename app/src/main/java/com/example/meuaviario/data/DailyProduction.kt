package com.example.meuaviario

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DailyProduction(
    @DocumentId val id: String = "", // Adicionado para identificar o documento
    val eggs: Int = 0,
    @ServerTimestamp val timestamp: Date? = null,
    val feedConsumed: Double = 0.0
)
