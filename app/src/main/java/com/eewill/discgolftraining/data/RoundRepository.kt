package com.eewill.discgolftraining.data

import kotlinx.coroutines.flow.Flow

class RoundRepository(private val dao: RoundDao) {
    suspend fun insertRound(round: RoundEntity, discIds: List<String> = emptyList()) {
        dao.insertRound(round)
        if (discIds.isNotEmpty()) {
            dao.insertRoundDiscs(
                discIds.mapIndexed { i, id -> RoundDiscEntity(roundId = round.id, discId = id, sortIndex = i) }
            )
        }
    }

    fun getRoundDiscs(roundId: String): Flow<List<DiscEntity>> = dao.getRoundDiscs(roundId)
    suspend fun getRoundDiscIdsOnce(roundId: String): List<String> = dao.getRoundDiscIdsOnce(roundId)
    suspend fun insertThrow(throwEntity: ThrowEntity) = dao.insertThrow(throwEntity)
    suspend fun deleteLastThrow(roundId: String) = dao.deleteLastThrow(roundId)
    suspend fun deleteThrow(id: String) = dao.deleteThrow(id)
    suspend fun deleteRound(id: String) = dao.deleteRound(id)
    suspend fun updateNotes(id: String, notes: String?) = dao.updateNotes(id, notes)
    suspend fun getRound(id: String): RoundEntity? = dao.getRound(id)
    fun getAllRounds(): Flow<List<RoundEntity>> = dao.getAllRounds()
    fun getRoundWithThrows(id: String): Flow<RoundWithThrows?> = dao.getRoundWithThrows(id)
    fun getAllRoundsWithCounts(): Flow<List<RoundSummary>> = dao.getAllRoundsWithCounts()
    fun getAllRoundStats(): Flow<List<RoundStatsRow>> = dao.getAllRoundStats()
    fun getGapStatsForDisc(discId: String): Flow<DiscGapStatsRow> = dao.getGapStatsForDisc(discId)
    fun getGapStatsGroupedByDisc(): Flow<List<DiscGapStatsByDiscRow>> = dao.getGapStatsGroupedByDisc()
    fun getShortDiscIds(roundId: String): Flow<List<String>> = dao.getShortDiscIds(roundId)
    suspend fun setDiscShort(roundId: String, discId: String, short: Boolean) {
        if (short) dao.insertShortDisc(RoundShortDiscEntity(roundId, discId))
        else dao.deleteShortDisc(roundId, discId)
    }
}
