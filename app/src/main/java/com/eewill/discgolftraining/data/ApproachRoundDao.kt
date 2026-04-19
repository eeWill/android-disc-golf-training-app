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
    val discType: DiscType?,
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
               d.type AS discType
        FROM approach_rounds r
        INNER JOIN approach_throws t ON t.roundId = r.id
        LEFT JOIN discs d ON d.id = t.discId
        ORDER BY r.createdAt ASC, t.`index` ASC
        """
    )
    fun getAllApproachThrowStats(): Flow<List<ApproachThrowStatsRow>>
}
