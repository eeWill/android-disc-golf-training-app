package com.eewill.discgolftraining.data

import kotlinx.coroutines.flow.Flow

class DiscRepository(private val dao: DiscDao) {
    suspend fun insertDisc(disc: DiscEntity) = dao.insertDisc(disc)
    suspend fun updateDisc(disc: DiscEntity) = dao.updateDisc(disc)
    suspend fun deleteDisc(id: String) = dao.deleteDisc(id)
    suspend fun getDisc(id: String): DiscEntity? = dao.getDisc(id)
    fun observeDisc(id: String): Flow<DiscEntity?> = dao.observeDisc(id)
    fun getAllDiscs(): Flow<List<DiscEntity>> = dao.getAllDiscs()
    fun getActiveDiscs(): Flow<List<DiscEntity>> = dao.getActiveDiscs()
    fun getDiscsForStats(): Flow<List<DiscEntity>> = dao.getDiscsForStats()
}
