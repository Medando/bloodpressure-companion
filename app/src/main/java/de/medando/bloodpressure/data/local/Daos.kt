package de.medando.bloodpressure.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Query("SELECT * FROM reading ORDER BY measured_at DESC, id DESC")
    fun observeAll(): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM reading ORDER BY measured_at DESC, id DESC LIMIT 1")
    fun observeLatest(): Flow<ReadingEntity?>

    @Query("SELECT * FROM reading WHERE id = :id")
    suspend fun get(id: Long): ReadingEntity?

    @Upsert
    suspend fun upsert(reading: ReadingEntity): Long

    @Query("DELETE FROM reading WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface DeviceDao {
    @Query("SELECT name FROM device")
    fun observeAll(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(device: DeviceEntity)

    @Query("DELETE FROM device WHERE name = :name")
    suspend fun delete(name: String)
}
