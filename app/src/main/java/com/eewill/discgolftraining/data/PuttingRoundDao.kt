package com.eewill.discgolftraining.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class PuttingRoundSummary(
    val id: String,
    val createdAt: Long,
    val minDistanceFeet: Float,
    val maxDistanceFeet: Float,
    val intervalFeet: Float,
    val throwsPerPosition: Int,
    val madeCount: Int,
    val attemptedCount: Int,
    val notes: String?,
)

data class PuttingThrowStatsRow(
    val roundId: String,
    val createdAt: Long,
    val distanceFeet: Float,
    val result: PuttingResult,
)

@Dao
interface PuttingRoundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: PuttingRoundEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThrow(throwEntity: PuttingThrowEntity)

    @Query("DELETE FROM putting_throws WHERE roundId = :roundId AND positionIndex = :positionIndex AND throwIndex = :throwIndex")
    suspend fun deleteThrowAt(roundId: String, positionIndex: Int, throwIndex: Int)

    @Query("UPDATE putting_rounds SET notes = :notes WHERE id = :roundId")
    suspend fun updateRoundNotes(roundId: String, notes: String?)

    @Query("DELETE FROM putting_rounds WHERE id = :id")
    suspend fun deleteRound(id: String)

    @Query("SELECT * FROM putting_rounds WHERE id = :id")
    suspend fun getRound(id: String): PuttingRoundEntity?

    @Query("SELECT * FROM putting_throws WHERE roundId = :roundId")
    fun getRoundThrows(roundId: String): Flow<List<PuttingThrowEntity>>

    @Query(
        """
        SELECT r.id AS id,
               r.createdAt AS createdAt,
               r.minDistanceFeet AS minDistanceFeet,
               r.maxDistanceFeet AS maxDistanceFeet,
               r.intervalFeet AS intervalFeet,
               r.throwsPerPosition AS throwsPerPosition,
               r.notes AS notes,
               COUNT(t.id) AS attemptedCount,
               SUM(CASE WHEN t.result = 'MADE' THEN 1 ELSE 0 END) AS madeCount
        FROM putting_rounds r
        LEFT JOIN putting_throws t ON t.roundId = r.id
        GROUP BY r.id
        ORDER BY r.createdAt DESC
        """
    )
    fun getAllPuttingRoundSummaries(): Flow<List<PuttingRoundSummary>>

    @Query(
        """
        SELECT t.roundId AS roundId,
               r.createdAt AS createdAt,
               t.distanceFeet AS distanceFeet,
               t.result AS result
        FROM putting_throws t
        INNER JOIN putting_rounds r ON r.id = t.roundId
        ORDER BY r.createdAt ASC
        """
    )
    fun getAllPuttingThrowStats(): Flow<List<PuttingThrowStatsRow>>
}
