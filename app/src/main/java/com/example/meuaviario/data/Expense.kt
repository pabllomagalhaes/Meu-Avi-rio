package com.example.meuaviario

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Expense(
    @DocumentId val id: String = "", // Adiciona esta linha
    val description: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    @ServerTimestamp val timestamp: Date? = null
)