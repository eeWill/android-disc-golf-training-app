package com.eewill.discgolftraining.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rounds")
data class RoundEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val imagePath: String,
    val distanceFeet: Float,
    val gapWidthFeet: Float,
    val gapLeft: Float,
    val gapTop: Float,
    val gapRight: Float,
    val gapBottom: Float,
    val discDataMode: DiscDataMode = DiscDataMode.NONE,
    val notes: String? = null,
)
