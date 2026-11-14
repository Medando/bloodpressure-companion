package de.medando.bloodpressure.data.repository

import de.medando.bloodpressure.data.local.DeviceDao
import de.medando.bloodpressure.data.local.DeviceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class RoomDeviceRepositoryTest {

    private class FakeDeviceDao : DeviceDao {
        val names = MutableStateFlow(emptyList<String>())
        override fun observeAll(): Flow<List<String>> = names
        override suspend fun insert(device: DeviceEntity) {
            if (device.name !in names.value) names.value += device.name
        }
        override suspend fun delete(name: String) {
            names.value -= name
        }
    }

    private val dao = FakeDeviceDao()
    private val repository = RoomDeviceRepository(dao)
    private val originalLocale = Locale.getDefault()

    @Before
    fun setLocale() = Locale.setDefault(Locale.GERMANY)

    @After
    fun restoreLocale() = Locale.setDefault(originalLocale)

    @Test
    fun `names are trimmed and blank names are ignored`() = runTest {
        repository.add(" Omron M500 ")
        repository.add("   ")

        assertEquals(listOf("Omron M500"), dao.names.value)
    }

    @Test
    fun `devices are sorted alphabetically ignoring case, umlauts in place`() = runTest {
        listOf("Zeta", "beurer", "Ärzte-Gerät", "Omron").forEach { repository.add(it) }

        assertEquals(listOf("Ärzte-Gerät", "beurer", "Omron", "Zeta"), repository.observeAll().first())
    }
}
