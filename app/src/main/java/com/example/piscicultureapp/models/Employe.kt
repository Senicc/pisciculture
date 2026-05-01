package com.example.piscicultureapp.models

data class Employe(
    val id: Int = 0,
    val nom: String,
    val prenom: String?,
    val role: String?,
    val telephone: String?
)