package de.medando.bloodpressure.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.medando.bloodpressure.domain.model.Guideline
import de.medando.bloodpressure.domain.model.Settings
import de.medando.bloodpressure.domain.model.TargetRange
import de.medando.bloodpressure.domain.model.TimesOfDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSettingsRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = TestScope(UnconfinedTestDispatcher())
    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(scope = scope.backgroundScope) { tmp.newFile("settings.preferences_pb") }
    }
    private val repository by lazy { DataStoreSettingsRepository(dataStore) }

    private val custom = Settings(
        firstDayOfWeek = DayOfWeek.SUNDAY,
        timesOfDay = TimesOfDay(
            morning = LocalTime.of(6, 30),
            afternoon = LocalTime.of(12, 0),
            evening = LocalTime.of(18, 15),
            night = LocalTime.of(22, 45),
        ),
        guideline = Guideline.ACC_AHA_2017,
        targetRange = TargetRange(maxSystolic = 130, maxDiastolic = 80),
        name = "Ada Lovelace",
        dateOfBirth = LocalDate.of(1960, 2, 29),
    )

    @Test
    fun `empty store yields defaults`() = scope.runTest {
        assertEquals(Settings(), repository.settings.first())
    }

    @Test
    fun `round trip keeps all fields`() = scope.runTest {
        repository.update { custom }

        assertEquals(custom, repository.settings.first())
    }

    @Test
    fun `update receives the current settings`() = scope.runTest {
        repository.update { custom }
        repository.update { it.copy(guideline = Guideline.ESH_2023) }

        assertEquals(custom.copy(guideline = Guideline.ESH_2023), repository.settings.first())
    }

    @Test
    fun `clearing nullable fields removes them`() = scope.runTest {
        repository.update { custom }
        repository.update { it.copy(firstDayOfWeek = null, targetRange = null, name = null, dateOfBirth = null) }

        val settings = repository.settings.first()
        assertNull(settings.firstDayOfWeek)
        assertNull(settings.targetRange)
        assertNull(settings.name)
        assertNull(settings.dateOfBirth)
        val storedKeys = dataStore.data.first().asMap().keys.map { it.name }
        assertTrue(storedKeys.none { it in setOf("first_day_of_week", "target_systolic", "target_diastolic", "name", "date_of_birth_epoch_day") })
    }

    @Test
    fun `blank name is stored as null and names are trimmed`() = scope.runTest {
        repository.update { it.copy(name = "   ") }
        assertNull(repository.settings.first().name)

        repository.update { it.copy(name = " Ada ") }
        assertEquals("Ada", repository.settings.first().name)
    }

    @Test
    fun `defaults are not persisted`() = scope.runTest {
        repository.update { it.copy(name = "Ada") }

        assertEquals(listOf("name"), dataStore.data.first().asMap().keys.map { it.name })
    }

    @Test
    fun `out-of-range stored values fall back to defaults`() = scope.runTest {
        dataStore.edit {
            it[intPreferencesKey("first_day_of_week")] = 9
            it[intPreferencesKey("time_of_day_morning")] = 5_000
            it[intPreferencesKey("time_of_day_night")] = -1
        }

        assertEquals(Settings(), repository.settings.first())
    }

    @Test
    fun `unknown stored guideline falls back to the default`() = scope.runTest {
        dataStore.edit { it[stringPreferencesKey("guideline")] = "WHO_1999" }

        assertEquals(Guideline.ESC_2024, repository.settings.first().guideline)
    }
}
