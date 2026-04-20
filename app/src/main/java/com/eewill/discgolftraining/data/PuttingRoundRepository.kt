package com.eewill.discgolftraining.data

import kotlinx.coroutines.flow.Flow

class PuttingRoundRepository(private val dao: PuttingRoundDao) {
    suspend fun createRound(
        id: String,
        minDistanceFeet: Float,
        maxDistanceFeet: Float,
        intervalFeet: Float,
        throwsPerPosition: Int,
    ) {
        dao.insertRound(
            PuttingRoundEntity(
                id = id,
                createdAt = System.currentTimeMillis(),
                minDistanceFeet = minDistanceFeet,
                maxDistanceFeet = maxDistanceFeet,
                intervalFeet = intervalFeet,
                throwsPerPosition = throwsPerPosition,
            )
        )
    }

    suspend fun upsertThrow(throwEntity: PuttingThrowEntity) = dao.upsertThrow(throwEntity)
    suspend fun deleteThrowAt(roundId: String, positionIndex: Int, throwIndex: Int) =
        dao.deleteThrowAt(roundId, positionIndex, throwIndex)
    suspend fun updateRoundNotes(roundId: String, notes: String?) =
        dao.updateRoundNotes(roundId, notes)
    suspend fun deleteRound(id: String) = dao.deleteRound(id)
    suspend fun getRound(id: String): PuttingRoundEntity? = dao.getRound(id)
    fun getRoundThrows(roundId: String): Flow<List<PuttingThrowEntity>> = dao.getRoundThrows(roundId)
    fun getAllPuttingRoundSummaries(): Flow<List<PuttingRoundSummary>> =
        dao.getAllPuttingRoundSummaries()
    fun getAllPuttingThrowStats(): Flow<List<PuttingThrowStatsRow>> =
        dao.getAllPuttingThrowStats()
}
