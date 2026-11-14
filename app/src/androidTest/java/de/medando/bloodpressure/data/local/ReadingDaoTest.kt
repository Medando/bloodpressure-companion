package de.medando.bloodpressure.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadingDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ReadingDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        dao = db.readingDao()
    }

    @After
    fun tearDown() = db.close()

    private fun entity(measuredAt: Long, systolic: Int = 120) = ReadingEntity(
        measuredAtEpochMillis = measuredAt,
        systolic = systolic,
        diastolic = 80,
        pulse = 70,
        site = null,
        position = null,
        device = null,
        sleepMinutes = null,
        comment = null,
    )

    @Test
    fun observeAll_returnsNewestFirst() = runTest {
        dao.upsert(entity(measuredAt = 1_000))
        dao.upsert(entity(measuredAt = 3_000))
        dao.upsert(entity(measuredAt = 2_000))

        dao.observeAll().test {
            assertEquals(listOf(3_000L, 2_000L, 1_000L), awaitItem().map { it.measuredAtEpochMillis })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeAll_ordersEqualTimestampsByNewestId() = runTest {
        val first = dao.upsert(entity(measuredAt = 1_000))
        val second = dao.upsert(entity(measuredAt = 1_000))

        dao.observeAll().test {
            assertEquals(listOf(second, first), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upsert_returnsNewIdOnInsertAndMinusOneOnUpdate() = runTest {
        val id = dao.upsert(entity(measuredAt = 1_000))
        val updateResult = dao.upsert(entity(measuredAt = 1_000, systolic = 135).copy(id = id))

        assertTrue(id > 0)
        assertEquals(-1L, updateResult)
        assertEquals(135, dao.get(id)?.systolic)
    }

    @Test
    fun delete_removesRow() = runTest {
        val id = dao.upsert(entity(measuredAt = 1_000))
        dao.delete(id)

        assertNull(dao.get(id))
    }

    @Test
    fun upsert_afterDelete_reinsertsUnderTheSameId() = runTest {
        val id = dao.upsert(entity(measuredAt = 1_000))
        dao.delete(id)

        assertEquals(id, dao.upsert(entity(measuredAt = 1_000).copy(id = id)))
        assertEquals(id, dao.get(id)?.id)
    }

    @Test
    fun observeLatest_returnsNewestByMeasuredAt() = runTest {
        dao.upsert(entity(measuredAt = 3_000, systolic = 133))
        dao.upsert(entity(measuredAt = 1_000, systolic = 111))

        dao.observeLatest().test {
            assertEquals(133, awaitItem()?.systolic)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeLatest_isNullWhenEmpty() = runTest {
        dao.observeLatest().test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
