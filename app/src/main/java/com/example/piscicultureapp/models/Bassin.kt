package com.example.piscicultureapp.models

data class Bassin(
    val id: Int = 0,
    val nomBassin: String,
    val capacite: Int,
    val typeBassin: String?,
    val localisation: String?,
    val etat: String = "actif"
)