package com.eewill.discgolftraining.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "approach_rounds")
data class ApproachRoundEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val targetDistanceFeet: Float,
)
