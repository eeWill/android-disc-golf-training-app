package com.eewill.discgolftraining.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "putting_rounds")
data class PuttingRoundEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val minDistanceFeet: Float,
    val maxDistanceFeet: Float,
    val intervalFeet: Float,
    val throwsPerPosition: Int,
    val notes: String? = null,
)
