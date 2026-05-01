package com.example.piscicultureapp.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormat {
    private val numberFr: NumberFormat = NumberFormat.getNumberInstance(Locale.FRANCE).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    /** Montant en ariary (MGA), affichage lisible type 1 234 567 */
    fun formatAriary(amount: Double): String {
        val n = numberFr.format(amount)
        return "$n Ar"
    }

    fun formatAriaryWithLabel(amount: Double): String {
        return "${formatAriary(amount)} · Ariary"
    }
}
