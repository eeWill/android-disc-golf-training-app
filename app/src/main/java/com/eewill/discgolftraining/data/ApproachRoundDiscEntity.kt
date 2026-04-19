package com.eewill.discgolftraining.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "approach_round_discs",
    primaryKeys = ["roundId", "discId"],
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
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("roundId"), Index("discId")],
)
data class ApproachRoundDiscEntity(
    val roundId: String,
    val discId: String,
    val sortIndex: Int,
)
