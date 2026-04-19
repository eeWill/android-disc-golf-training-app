package com.eewill.discgolftraining.data

import kotlinx.coroutines.flow.Flow

class ApproachRoundRepository(private val dao: ApproachRoundDao) {
    suspend fun createRound(id: String, targetDistanceFeet: Float, discIds: List<String>) {
        dao.insertRound(
            ApproachRoundEntity(
                id = id,
                createdAt = System.currentTimeMillis(),
                targetDistanceFeet = targetDistanceFeet,
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
    suspend fun deleteRound(id: String) = dao.deleteRound(id)
    suspend fun getRound(id: String): ApproachRoundEntity? = dao.getRound(id)
    fun getRoundDiscs(roundId: String): Flow<List<DiscEntity>> = dao.getRoundDiscs(roundId)
    fun getRoundThrows(roundId: String): Flow<List<ApproachThrowEntity>> = dao.getRoundThrows(roundId)
    fun getAllApproachThrowStats(): Flow<List<ApproachThrowStatsRow>> = dao.getAllApproachThrowStats()
}
