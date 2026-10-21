package com.example.domain.model

data class Game(
    val id: String,
    val name: String,
    val namespace: String,
    val imageUrl: String?,
    val developer: String?,
    val description: String?,
    val ownedSince: Long
)
