package de.medando.bloodpressure.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReadingEntity::class, DeviceEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao
    abstract fun deviceDao(): DeviceDao

    companion object {
        const val NAME = "bloodpressure.db"
    }
}
