package com.example.meuaviario

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa uma única despesa.
 *
 * @property description Descrição da despesa.
 * @property amount Valor da despesa.
 * @property category Categoria da despesa (ex: Ração, Saúde).
 * @property timestamp Data do registro.
 */
data class Expense(
    val description: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    @ServerTimestamp val timestamp: Date? = null
)
