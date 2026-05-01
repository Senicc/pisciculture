package com.example.piscicultureapp.models

data class Espece(
    val id: Int = 0,
    val nomEspece: String,
    val description: String?,
    val prixUnitaire: Float = 0.0f
)