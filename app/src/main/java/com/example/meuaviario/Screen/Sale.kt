package com.example.meuaviario

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa uma única venda de ovos.
 *
 * @property quantityInDozens Quantidade de dúzias vendidas.
 * @property pricePerDozen Preço por dúzia.
 * @property totalAmount Valor total da venda.
 * @property timestamp Data da venda.
 */
data class Sale(
    val quantityInDozens: Int = 0,
    val pricePerDozen: Double = 0.0,
    val totalAmount: Double = 0.0,
    @ServerTimestamp val timestamp: Date? = null
)
