package com.example.piscicultureapp.models

data class Poisson(
    val id: Int = 0,
    val quantite: Int,
    val dateIntroduction: String?,      // nullable OK
    val poidsMoyen: Float?,             // nullable OK
    val mortalite: Int = 0,
    val idEspece: Int,
    val idBassin: Int,
    val nomEspece: String = "",
    val nomBassin: String = ""
)