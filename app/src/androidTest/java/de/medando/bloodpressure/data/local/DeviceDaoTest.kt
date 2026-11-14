package de.medando.bloodpressure.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: DeviceDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        dao = db.deviceDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insert_ignoresDuplicates() = runTest {
        dao.insert(DeviceEntity("Omron"))
        dao.insert(DeviceEntity("Omron"))

        assertEquals(listOf("Omron"), dao.observeAll().first())
    }

    @Test
    fun delete_removesDevice() = runTest {
        dao.insert(DeviceEntity("Omron"))
        dao.insert(DeviceEntity("Beurer"))
        dao.delete("Omron")

        assertEquals(listOf("Beurer"), dao.observeAll().first())
    }
}
