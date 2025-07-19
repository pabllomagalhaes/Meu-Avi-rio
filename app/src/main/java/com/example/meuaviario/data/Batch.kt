package com.example.meuaviario.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa um lote de galinhas.
 *
 * @property name Nome do lote (ex: "Lote A - Embrapa 051").
 * @property breed Raça das galinhas.
 * @property numberOfHens Quantidade de galinhas no lote.
 * @property startDate Data de início (alojamento) do lote.
 */
data class Batch(
    val name: String = "",
    val breed: String = "",
    val numberOfHens: Int = 0,
    @ServerTimestamp val startDate: Date? = null
)