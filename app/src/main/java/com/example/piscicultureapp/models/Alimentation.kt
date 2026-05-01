package com.example.piscicultureapp.models

data class Alimentation(
    val id: Int = 0,
    val nomAliment: String,
    val typeAliment: String?,
    val stock: Float
)