package com.eewill.discgolftraining.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisc(disc: DiscEntity)

    @Update
    suspend fun updateDisc(disc: DiscEntity)

    @Query("DELETE FROM discs WHERE id = :id")
    suspend fun deleteDisc(id: String)

    @Query("SELECT * FROM discs WHERE id = :id")
    suspend fun getDisc(id: String): DiscEntity?

    @Query("SELECT * FROM discs WHERE id = :id")
    fun observeDisc(id: String): Flow<DiscEntity?>

    @Query("SELECT * FROM discs ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllDiscs(): Flow<List<DiscEntity>>

    @Query("SELECT * FROM discs WHERE isActive = 1 ORDER BY sortOrder ASC, createdAt ASC")
    fun getActiveDiscs(): Flow<List<DiscEntity>>

    @Query("SELECT * FROM discs WHERE includeInStats = 1 ORDER BY sortOrder ASC, createdAt ASC")
    fun getDiscsForStats(): Flow<List<DiscEntity>>
}
