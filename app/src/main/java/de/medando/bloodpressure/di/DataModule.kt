package de.medando.bloodpressure.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.medando.bloodpressure.data.local.AppDatabase
import de.medando.bloodpressure.data.repository.DataStoreSettingsRepository
import de.medando.bloodpressure.data.repository.RoomDeviceRepository
import de.medando.bloodpressure.data.repository.RoomReadingRepository
import de.medando.bloodpressure.domain.repository.DeviceRepository
import de.medando.bloodpressure.domain.repository.ReadingRepository
import de.medando.bloodpressure.domain.repository.SettingsRepository
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()

    @Provides
    fun readingDao(db: AppDatabase) = db.readingDao()

    @Provides
    fun deviceDao(db: AppDatabase) = db.deviceDao()

    @Provides
    @Singleton
    fun preferences(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            // A corrupt file falls back to defaults instead of crashing every start.
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        ) { context.preferencesDataStoreFile("settings") }

    /** For instants only; resolve the time zone where a local date or time is needed. */
    @Provides
    fun clock(): Clock = Clock.systemUTC()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun readings(impl: RoomReadingRepository): ReadingRepository
    @Binds abstract fun devices(impl: RoomDeviceRepository): DeviceRepository
    @Binds abstract fun settings(impl: DataStoreSettingsRepository): SettingsRepository
}
