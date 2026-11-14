package de.medando.bloodpressure.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.medando.bloodpressure.domain.model.Guideline
import de.medando.bloodpressure.domain.model.Settings
import de.medando.bloodpressure.domain.model.TargetRange
import de.medando.bloodpressure.domain.model.TimesOfDay
import de.medando.bloodpressure.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<Settings> = dataStore.data.map { it.toSettings() }

    override suspend fun update(transform: (Settings) -> Settings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toSettings())) }
    }

    private companion object {
        val DEFAULTS = Settings()
        const val MINUTES_PER_DAY = 24 * 60
    }

    private object Keys {
        val firstDayOfWeek = intPreferencesKey("first_day_of_week")
        val morning = intPreferencesKey("time_of_day_morning")
        val afternoon = intPreferencesKey("time_of_day_afternoon")
        val evening = intPreferencesKey("time_of_day_evening")
        val night = intPreferencesKey("time_of_day_night")
        val guideline = stringPreferencesKey("guideline")
        val targetSystolic = intPreferencesKey("target_systolic")
        val targetDiastolic = intPreferencesKey("target_diastolic")
        val name = stringPreferencesKey("name")
        val dateOfBirth = longPreferencesKey("date_of_birth_epoch_day")
    }

    private fun Preferences.toSettings(): Settings {
        val defaults = DEFAULTS.timesOfDay
        // Out-of-range values (e.g. from a foreign backup) fall back to the default.
        fun time(key: Preferences.Key<Int>, fallback: LocalTime) =
            this[key]?.takeIf { it in 0 until MINUTES_PER_DAY }?.let { LocalTime.ofSecondOfDay(it * 60L) } ?: fallback
        val sys = this[Keys.targetSystolic]
        val dia = this[Keys.targetDiastolic]
        return Settings(
            firstDayOfWeek = this[Keys.firstDayOfWeek]?.let { DayOfWeek.entries.getOrNull(it - 1) },
            timesOfDay = TimesOfDay(
                morning = time(Keys.morning, defaults.morning),
                afternoon = time(Keys.afternoon, defaults.afternoon),
                evening = time(Keys.evening, defaults.evening),
                night = time(Keys.night, defaults.night),
            ),
            guideline = this[Keys.guideline]
                ?.let { stored -> Guideline.entries.firstOrNull { it.name == stored } }
                ?: DEFAULTS.guideline,
            targetRange = if (sys != null && dia != null) TargetRange(sys, dia) else null,
            name = this[Keys.name],
            dateOfBirth = this[Keys.dateOfBirth]?.let(LocalDate::ofEpochDay),
        )
    }

    private fun MutablePreferences.write(s: Settings) {
        fun <T> set(key: Preferences.Key<T>, value: T?) {
            if (value == null) remove(key) else this[key] = value
        }
        // Defaults are not persisted, so a changed default reaches users who never chose a value.
        fun minutes(t: LocalTime, default: LocalTime) = t.takeIf { it != default }?.let { it.toSecondOfDay() / 60 }
        val defaults = DEFAULTS.timesOfDay
        set(Keys.firstDayOfWeek, s.firstDayOfWeek?.value)
        set(Keys.morning, minutes(s.timesOfDay.morning, defaults.morning))
        set(Keys.afternoon, minutes(s.timesOfDay.afternoon, defaults.afternoon))
        set(Keys.evening, minutes(s.timesOfDay.evening, defaults.evening))
        set(Keys.night, minutes(s.timesOfDay.night, defaults.night))
        set(Keys.guideline, s.guideline.takeIf { it != DEFAULTS.guideline }?.name)
        set(Keys.targetSystolic, s.targetRange?.maxSystolic)
        set(Keys.targetDiastolic, s.targetRange?.maxDiastolic)
        set(Keys.name, s.name?.trim()?.takeIf { it.isNotEmpty() })
        set(Keys.dateOfBirth, s.dateOfBirth?.toEpochDay())
    }
}
