package com.example.meuaviario

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa um lote de galinhas.
 *
 * @property id O ID único do documento no Firestore.
 * @property name Nome do lote (ex: "Lote A - Embrapa 051").
 * @property breed Raça das galinhas.
 * @property numberOfHens Quantidade de galinhas no lote.
 * @property startDate Data de início (alojamento) do lote.
 */
data class Batch(
    @DocumentId val id: String = "", // O Firestore preencherá isto automaticamente
    val name: String = "",
    val breed: String = "",
    val numberOfHens: Int = 0,
    @ServerTimestamp val startDate: Date? = null
)
