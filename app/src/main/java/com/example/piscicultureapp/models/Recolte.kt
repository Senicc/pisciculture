package com.example.piscicultureapp.models

data class Recolte(
    val id: Int = 0,
    val dateRecolte: String?,
    val quantite: Int,
    val poidsTotal: Float,
    val idBassin: Int,
    // Pour affichage plus lisible
    val nomBassin: String = ""
)