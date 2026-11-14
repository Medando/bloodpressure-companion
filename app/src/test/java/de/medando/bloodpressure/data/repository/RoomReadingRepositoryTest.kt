package de.medando.bloodpressure.data.repository

import de.medando.bloodpressure.data.local.ReadingDao
import de.medando.bloodpressure.data.local.ReadingEntity
import de.medando.bloodpressure.domain.model.Reading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class RoomReadingRepositoryTest {

    /** Mimics Room's @Upsert: row id on insert (generated when the id is 0), -1 on update. */
    private class FakeReadingDao : ReadingDao {
        val rows = mutableMapOf<Long, ReadingEntity>()
        override fun observeAll(): Flow<List<ReadingEntity>> = emptyFlow()
        override fun observeLatest(): Flow<ReadingEntity?> = emptyFlow()
        override suspend fun get(id: Long) = rows[id]
        override suspend fun delete(id: Long) { rows.remove(id) }
        override suspend fun upsert(reading: ReadingEntity): Long =
            if (reading.id in rows) {
                rows[reading.id] = reading
                -1
            } else {
                val id = reading.id.takeIf { it != 0L } ?: ((rows.keys.maxOrNull() ?: 0) + 1)
                rows[id] = reading.copy(id = id)
                id
            }
    }

    private val dao = FakeReadingDao()
    private val repository = RoomReadingRepository(dao)
    private val reading = Reading(measuredAt = Instant.EPOCH, systolic = 120, diastolic = 80, pulse = 70)

    @Test
    fun `save returns the generated id on insert`() = runTest {
        assertEquals(1L, repository.save(reading))
    }

    @Test
    fun `save returns the existing id on update`() = runTest {
        val id = repository.save(reading)

        assertEquals(id, repository.save(reading.copy(id = id, systolic = 135)))
        assertEquals(135, repository.get(id)?.systolic)
    }

    @Test
    fun `save after delete restores the reading under its id`() = runTest {
        val id = repository.save(reading)
        repository.delete(id)

        assertEquals(id, repository.save(reading.copy(id = id)))
        assertEquals(reading.copy(id = id), repository.get(id))
    }
}
