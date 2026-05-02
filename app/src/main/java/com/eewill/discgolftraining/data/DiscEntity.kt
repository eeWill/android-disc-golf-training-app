package com.eewill.discgolftraining.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discs")
data class DiscEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: DiscType,
    val createdAt: Long,
    val sortOrder: Long = 0L,
    val notes: String? = null,
    val isActive: Boolean = true,
    val includeInStats: Boolean = true,
)
