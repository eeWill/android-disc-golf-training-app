package com.eewill.discgolftraining.data

import kotlinx.coroutines.flow.Flow

class RoundRepository(private val dao: RoundDao) {
    suspend fun insertRound(round: RoundEntity) = dao.insertRound(round)
    suspend fun insertThrow(throwEntity: ThrowEntity) = dao.insertThrow(throwEntity)
    suspend fun deleteLastThrow(roundId: String) = dao.deleteLastThrow(roundId)
    suspend fun deleteRound(id: String) = dao.deleteRound(id)
    suspend fun updateNotes(id: String, notes: String?) = dao.updateNotes(id, notes)
    suspend fun getRound(id: String): RoundEntity? = dao.getRound(id)
    fun getAllRounds(): Flow<List<RoundEntity>> = dao.getAllRounds()
    fun getRoundWithThrows(id: String): Flow<RoundWithThrows?> = dao.getRoundWithThrows(id)
    fun getAllRoundsWithCounts(): Flow<List<RoundSummary>> = dao.getAllRoundsWithCounts()
    fun getAllRoundStats(): Flow<List<RoundStatsRow>> = dao.getAllRoundStats()
    fun getGapStatsForDisc(discId: String): Flow<DiscGapStatsRow> = dao.getGapStatsForDisc(discId)
    fun getGapStatsGroupedByDisc(): Flow<List<DiscGapStatsByDiscRow>> = dao.getGapStatsGroupedByDisc()
}
