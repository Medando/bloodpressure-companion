package de.medando.bloodpressure.data.repository

import de.medando.bloodpressure.data.local.DeviceDao
import de.medando.bloodpressure.data.local.DeviceEntity
import de.medando.bloodpressure.data.local.ReadingDao
import de.medando.bloodpressure.data.local.toDomain
import de.medando.bloodpressure.data.local.toEntity
import de.medando.bloodpressure.domain.model.Reading
import de.medando.bloodpressure.domain.repository.DeviceRepository
import de.medando.bloodpressure.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.Collator
import javax.inject.Inject

class RoomReadingRepository @Inject constructor(
    private val dao: ReadingDao,
) : ReadingRepository {
    override fun observeAll(): Flow<List<Reading>> = dao.observeAll().map { list -> list.map { it.toDomain() } }
    override fun observeLatest(): Flow<Reading?> = dao.observeLatest().map { it?.toDomain() }
    override suspend fun get(id: Long): Reading? = dao.get(id)?.toDomain()

    override suspend fun save(reading: Reading): Long {
        val rowId = dao.upsert(reading.toEntity())
        // Upsert returns -1 when it updated an existing row.
        return if (rowId == -1L) reading.id else rowId
    }

    override suspend fun delete(id: Long) = dao.delete(id)
}

class RoomDeviceRepository @Inject constructor(
    private val dao: DeviceDao,
) : DeviceRepository {
    // Sorted here, not in SQL: SQLite's NOCASE is ASCII-only and would misplace umlauts.
    override fun observeAll(): Flow<List<String>> = dao.observeAll().map { it.sortedWith(Collator.getInstance()) }

    override suspend fun add(name: String) {
        name.trim().takeIf { it.isNotEmpty() }?.let { dao.insert(DeviceEntity(it)) }
    }

    override suspend fun delete(name: String) = dao.delete(name)
}
