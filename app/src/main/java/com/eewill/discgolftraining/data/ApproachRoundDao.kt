package com.eewill.discgolftraining.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class ApproachThrowStatsRow(
    val roundId: String,
    val createdAt: Long,
    val distanceFeet: Float,
    val targetDistanceFeet: Float,
    val discType: DiscType?,
)

data class ApproachRoundSummary(
    val id: String,
    val createdAt: Long,
    val targetDistanceFeet: Float,
    val throwCount: Int,
    val avgDistanceFeet: Float?,
    val notes: String?,
)

data class DiscApproachStatsRow(
    val throwCount: Int,
    val avgDistanceFeet: Float?,
)

data class DiscApproachStatsByDiscRow(
    val discId: String,
    val throwCount: Int,
    val avgDistanceFeet: Float?,
)

@Dao
interface ApproachRoundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: ApproachRoundEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoundDiscs(discs: List<ApproachRoundDiscEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThrow(throwEntity: ApproachThrowEntity)

    @Query("DELETE FROM approach_throws WHERE roundId = :roundId AND discId = :discId")
    suspend fun deleteThrowForDisc(roundId: String, discId: String)

    @Query("UPDATE approach_rounds SET targetLat = :lat, targetLng = :lng WHERE id = :roundId")
    suspend fun updateRoundTarget(roundId: String, lat: Double?, lng: Double?)

    @Query("UPDATE approach_rounds SET startLat = :lat, startLng = :lng WHERE id = :roundId")
    suspend fun updateRoundStart(roundId: String, lat: Double?, lng: Double?)

    @Query("UPDATE approach_rounds SET notes = :notes WHERE id = :roundId")
    suspend fun updateRoundNotes(roundId: String, notes: String?)

    @Query("DELETE FROM approach_rounds WHERE id = :id")
    suspend fun deleteRound(id: String)

    @Query("SELECT * FROM approach_rounds WHERE id = :id")
    suspend fun getRound(id: String): ApproachRoundEntity?

    @Query(
        """
        SELECT d.* FROM discs d
        INNER JOIN approach_round_discs ard ON ard.discId = d.id
        WHERE ard.roundId = :roundId
        ORDER BY ard.sortIndex ASC
        """
    )
    fun getRoundDiscs(roundId: String): Flow<List<DiscEntity>>

    @Query("SELECT * FROM approach_throws WHERE roundId = :roundId ORDER BY `index` ASC")
    fun getRoundThrows(roundId: String): Flow<List<ApproachThrowEntity>>

    @Query(
        """
        SELECT r.id AS roundId,
               r.createdAt AS createdAt,
               t.landingDistanceFeet AS distanceFeet,
               r.targetDistanceFeet AS targetDistanceFeet,
               d.type AS discType
        FROM approach_rounds r
        INNER JOIN approach_throws t ON t.roundId = r.id
        LEFT JOIN discs d ON d.id = t.discId
        ORDER BY r.createdAt ASC, t.`index` ASC
        """
    )
    fun getAllApproachThrowStats(): Flow<List<ApproachThrowStatsRow>>

    @Query(
        """
        SELECT r.id AS id,
               r.createdAt AS createdAt,
               r.targetDistanceFeet AS targetDistanceFeet,
               r.notes AS notes,
               COUNT(t.id) AS throwCount,
               AVG(t.landingDistanceFeet) AS avgDistanceFeet
        FROM approach_rounds r
        LEFT JOIN approach_throws t ON t.roundId = r.id
        GROUP BY r.id
        ORDER BY r.createdAt DESC
        """
    )
    fun getAllApproachRoundSummaries(): Flow<List<ApproachRoundSummary>>

    @Query(
        """
        SELECT COUNT(t.id) AS throwCount,
               AVG(t.landingDistanceFeet) AS avgDistanceFeet
        FROM approach_throws t
        WHERE t.discId = :discId
        """
    )
    fun getApproachStatsForDisc(discId: String): Flow<DiscApproachStatsRow>

    @Query(
        """
        SELECT t.discId AS discId,
               COUNT(t.id) AS throwCount,
               AVG(t.landingDistanceFeet) AS avgDistanceFeet
        FROM approach_throws t
        WHERE t.discId IS NOT NULL
        GROUP BY t.discId
        """
    )
    fun getApproachStatsGroupedByDisc(): Flow<List<DiscApproachStatsByDiscRow>>
}
