package de.medando.bloodpressure.domain.repository

import de.medando.bloodpressure.domain.model.Reading
import de.medando.bloodpressure.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface ReadingRepository {
    /** Newest first. */
    fun observeAll(): Flow<List<Reading>>
    fun observeLatest(): Flow<Reading?>
    suspend fun get(id: Long): Reading?
    /**
     * Inserts with a generated id when [Reading.id] is 0. Otherwise updates the row with that id,
     * or inserts it under that id if it no longer exists (undo of a delete). Returns the id.
     */
    suspend fun save(reading: Reading): Long
    suspend fun delete(id: Long)
}

interface DeviceRepository {
    /** Alphabetical. */
    fun observeAll(): Flow<List<String>>
    suspend fun add(name: String)
    suspend fun delete(name: String)
}

interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun update(transform: (Settings) -> Settings)
}
