package com.eewill.discgolftraining.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "approach_rounds")
data class ApproachRoundEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val targetDistanceFeet: Float,
    val targetLat: Double? = null,
    val targetLng: Double? = null,
    val targetSizeFeet: Float? = null,
    val startLat: Double? = null,
    val startLng: Double? = null,
    val notes: String? = null,
)
