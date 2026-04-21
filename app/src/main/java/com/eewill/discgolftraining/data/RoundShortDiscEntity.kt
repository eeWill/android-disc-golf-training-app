package com.eewill.discgolftraining.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "round_short_discs",
    primaryKeys = ["roundId", "discId"],
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
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("roundId"), Index("discId")],
)
data class RoundShortDiscEntity(
    val roundId: String,
    val discId: String,
)
