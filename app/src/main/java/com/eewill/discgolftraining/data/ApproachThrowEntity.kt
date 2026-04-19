package com.eewill.discgolftraining.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "approach_throws",
    foreignKeys = [
        ForeignKey(
            entity = ApproachRoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DiscEntity::class,
            parentColumns = ["id"],
            childColumns = ["discId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("roundId"), Index("discId")],
)
data class ApproachThrowEntity(
    @PrimaryKey val id: String,
    val roundId: String,
    val index: Int,
    val discId: String?,
    val landingDistanceFeet: Float,
)
