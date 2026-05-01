package com.example.piscicultureapp.models

data class Nourrissage(
    val id: Int = 0,
    val dateNourrissage: String?,
    val quantite: Float,
    val idBassin: Int,
    val idAliment: Int,
    // Pour affichage plus lisible
    val nomBassin: String = "",
    val nomAliment: String = ""
)