package com.eewill.discgolftraining.data

import kotlinx.coroutines.flow.Flow

class ApproachRoundRepository(private val dao: ApproachRoundDao) {
    suspend fun createRound(
        id: String,
        targetDistanceFeet: Float,
        discIds: List<String>,
        targetLat: Double? = null,
        targetLng: Double? = null,
        targetSizeFeet: Float? = null,
        startLat: Double? = null,
        startLng: Double? = null,
    ) {
        dao.insertRound(
            ApproachRoundEntity(
                id = id,
                createdAt = System.currentTimeMillis(),
                targetDistanceFeet = targetDistanceFeet,
                targetLat = targetLat,
                targetLng = targetLng,
                targetSizeFeet = targetSizeFeet,
                startLat = startLat,
                startLng = startLng,
            )
        )
        dao.insertRoundDiscs(
            discIds.mapIndexed { i, discId ->
                ApproachRoundDiscEntity(roundId = id, discId = discId, sortIndex = i)
            }
        )
    }

    suspend fun insertThrow(throwEntity: ApproachThrowEntity) = dao.insertThrow(throwEntity)
    suspend fun deleteThrowForDisc(roundId: String, discId: String) =
        dao.deleteThrowForDisc(roundId, discId)
    suspend fun updateRoundTarget(roundId: String, lat: Double?, lng: Double?) =
        dao.updateRoundTarget(roundId, lat, lng)
    suspend fun updateRoundStart(roundId: String, lat: Double?, lng: Double?) =
        dao.updateRoundStart(roundId, lat, lng)
    suspend fun updateRoundNotes(roundId: String, notes: String?) =
        dao.updateRoundNotes(roundId, notes)
    suspend fun deleteRound(id: String) = dao.deleteRound(id)
    suspend fun getRound(id: String): ApproachRoundEntity? = dao.getRound(id)
    fun getRoundDiscs(roundId: String): Flow<List<DiscEntity>> = dao.getRoundDiscs(roundId)
    fun getRoundThrows(roundId: String): Flow<List<ApproachThrowEntity>> = dao.getRoundThrows(roundId)
    fun getAllApproachThrowStats(): Flow<List<ApproachThrowStatsRow>> = dao.getAllApproachThrowStats()
    fun getAllApproachRoundSummaries(): Flow<List<ApproachRoundSummary>> =
        dao.getAllApproachRoundSummaries()
    fun getApproachStatsForDisc(discId: String): Flow<DiscApproachStatsRow> =
        dao.getApproachStatsForDisc(discId)
    fun getApproachStatsGroupedByDisc(): Flow<List<DiscApproachStatsByDiscRow>> =
        dao.getApproachStatsGroupedByDisc()
}
