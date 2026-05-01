package com.example.piscicultureapp.models

data class Vente(
    val id: Int = 0,
    val client: String,
    val quantite: Int,
    val idEspece: Int,
    val prixUnitaire: Float,
    val prixTotal: Float,
    val dateVente: String?,
    // Pour affichage plus lisible
    val nomEspece: String = ""
)