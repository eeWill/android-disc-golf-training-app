package com.eewill.discgolftraining.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PuttingResult { MADE, MISSED }

@Entity(
    tableName = "putting_throws",
    foreignKeys = [
        ForeignKey(
            entity = PuttingRoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("roundId")],
)
data class PuttingThrowEntity(
    @PrimaryKey val id: String,
    val roundId: String,
    val distanceFeet: Float,
    val positionIndex: Int,
    val throwIndex: Int,
    val result: PuttingResult,
) {
    companion object {
        fun idFor(roundId: String, positionIndex: Int, throwIndex: Int): String =
            "$roundId:$positionIndex:$throwIndex"
    }
}
