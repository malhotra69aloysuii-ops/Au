package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Game

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val namespace: String,
    val imageUrl: String?,
    val developer: String?,
    val description: String?,
    val ownedSince: Long
)

fun GameEntity.toDomain(): Game = Game(
    id = id,
    name = name,
    namespace = namespace,
    imageUrl = imageUrl,
    developer = developer,
    description = description,
    ownedSince = ownedSince
)

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    name = name,
    namespace = namespace,
    imageUrl = imageUrl,
    developer = developer,
    description = description,
    ownedSince = ownedSince
)
