package com.example.meuaviario

/**
 * Representa a estrutura de dados para o resumo do aviário.
 *
 * @property activeHens O número de galinhas ativas.
 * @property eggsToday O número de ovos coletados hoje.
 * @property totalEggsLast7Days O número total de ovos coletados nos últimos 7 dias.
 * @property feedConsumedToday O consumo de ração do dia em kg.
 */
data class AviarySummary(
    val activeHens: Int = 0,
    val eggsToday: Int = 0,
    val totalEggsLast7Days: Int = 0,
    val feedConsumedToday: Double = 0.0
)
