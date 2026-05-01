package com.example.piscicultureapp.models

data class QualiteEau(
    val id: Int = 0,
    val temperature: Float,
    val ph: Float,
    val oxygene: Float,
    val dateMesure: String?,
    val idBassin: Int,
    // Pour affichage
    val nomBassin: String = ""
)