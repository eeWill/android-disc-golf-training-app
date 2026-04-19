package com.eewill.discgolftraining.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "throws",
    foreignKeys = [
        ForeignKey(
            entity = RoundEntity::class,
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
data class ThrowEntity(
    @PrimaryKey val id: String,
    val roundId: String,
    val index: Int,
    val x: Float,
    val y: Float,
    val isHit: Boolean,
    val discType: DiscType? = null,
    val discId: String? = null,
)
