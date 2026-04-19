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

    @Query("SELECT * FROM discs ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllDiscs(): Flow<List<DiscEntity>>
}
