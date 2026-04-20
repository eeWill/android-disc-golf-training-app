package com.eewill.discgolftraining.data

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class RoundWithThrows(
    @Embedded val round: RoundEntity,
    @Relation(parentColumn = "id", entityColumn = "roundId")
    val throws: List<ThrowEntity>,
)

data class RoundSummary(
    val id: String,
    val createdAt: Long,
    val imagePath: String,
    val hits: Int,
    val misses: Int,
    val notes: String?,
)

data class RoundStatsRow(
    val id: String,
    val createdAt: Long,
    val distanceFeet: Float,
    val gapWidthFeet: Float,
    val hits: Int,
    val misses: Int,
    val missLeft: Int,
    val missRight: Int,
    val missHigh: Int,
    val missLow: Int,
)

data class DiscGapStatsRow(
    val hits: Int,
    val misses: Int,
    val missLeft: Int,
    val missRight: Int,
    val missHigh: Int,
    val missLow: Int,
)

data class DiscGapStatsByDiscRow(
    val discId: String,
    val hits: Int,
    val misses: Int,
    val missLeft: Int,
    val missRight: Int,
    val missHigh: Int,
    val missLow: Int,
)

@Dao
interface RoundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: RoundEntity)

    @Update
    suspend fun updateRound(round: RoundEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThrow(throwEntity: ThrowEntity)

    @Query(
        """
        DELETE FROM throws
        WHERE roundId = :roundId
          AND `index` = (SELECT MAX(`index`) FROM throws WHERE roundId = :roundId)
        """
    )
    suspend fun deleteLastThrow(roundId: String)

    @Query("DELETE FROM rounds WHERE id = :id")
    suspend fun deleteRound(id: String)

    @Query("UPDATE rounds SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String?)

    @Query("SELECT * FROM rounds WHERE id = :id")
    suspend fun getRound(id: String): RoundEntity?

    @Query("SELECT * FROM rounds ORDER BY createdAt DESC")
    fun getAllRounds(): Flow<List<RoundEntity>>

    @Transaction
    @Query("SELECT * FROM rounds WHERE id = :id")
    fun getRoundWithThrows(id: String): Flow<RoundWithThrows?>

    @Query(
        """
        SELECT r.id AS id,
               r.createdAt AS createdAt,
               r.imagePath AS imagePath,
               r.notes AS notes,
               COALESCE(SUM(CASE WHEN t.isHit THEN 1 ELSE 0 END), 0) AS hits,
               COALESCE(SUM(CASE WHEN t.isHit THEN 0 ELSE 1 END), 0) AS misses
        FROM rounds r
        LEFT JOIN throws t ON t.roundId = r.id
        GROUP BY r.id
        ORDER BY r.createdAt DESC
        """
    )
    fun getAllRoundsWithCounts(): Flow<List<RoundSummary>>

    @Query(
        """
        SELECT r.id AS id,
               r.createdAt AS createdAt,
               r.distanceFeet AS distanceFeet,
               r.gapWidthFeet AS gapWidthFeet,
               COALESCE(SUM(CASE WHEN t.isHit THEN 1 ELSE 0 END), 0) AS hits,
               COALESCE(SUM(CASE WHEN t.isHit THEN 0 ELSE 1 END), 0) AS misses,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.x < r.gapLeft   THEN 1 ELSE 0 END), 0) AS missLeft,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.x > r.gapRight  THEN 1 ELSE 0 END), 0) AS missRight,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.y < r.gapTop    THEN 1 ELSE 0 END), 0) AS missHigh,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.y > r.gapBottom THEN 1 ELSE 0 END), 0) AS missLow
        FROM rounds r
        LEFT JOIN throws t ON t.roundId = r.id
        GROUP BY r.id
        ORDER BY r.createdAt ASC
        """
    )
    fun getAllRoundStats(): Flow<List<RoundStatsRow>>

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN t.isHit THEN 1 ELSE 0 END), 0) AS hits,
               COALESCE(SUM(CASE WHEN t.isHit THEN 0 ELSE 1 END), 0) AS misses,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.x < r.gapLeft   THEN 1 ELSE 0 END), 0) AS missLeft,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.x > r.gapRight  THEN 1 ELSE 0 END), 0) AS missRight,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.y < r.gapTop    THEN 1 ELSE 0 END), 0) AS missHigh,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.y > r.gapBottom THEN 1 ELSE 0 END), 0) AS missLow
        FROM throws t
        INNER JOIN rounds r ON r.id = t.roundId
        WHERE t.discId = :discId
        """
    )
    fun getGapStatsForDisc(discId: String): Flow<DiscGapStatsRow>

    @Query(
        """
        SELECT t.discId AS discId,
               COALESCE(SUM(CASE WHEN t.isHit THEN 1 ELSE 0 END), 0) AS hits,
               COALESCE(SUM(CASE WHEN t.isHit THEN 0 ELSE 1 END), 0) AS misses,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.x < r.gapLeft   THEN 1 ELSE 0 END), 0) AS missLeft,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.x > r.gapRight  THEN 1 ELSE 0 END), 0) AS missRight,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.y < r.gapTop    THEN 1 ELSE 0 END), 0) AS missHigh,
               COALESCE(SUM(CASE WHEN t.isHit = 0 AND t.y > r.gapBottom THEN 1 ELSE 0 END), 0) AS missLow
        FROM throws t
        INNER JOIN rounds r ON r.id = t.roundId
        WHERE t.discId IS NOT NULL
        GROUP BY t.discId
        """
    )
    fun getGapStatsGroupedByDisc(): Flow<List<DiscGapStatsByDiscRow>>
}
