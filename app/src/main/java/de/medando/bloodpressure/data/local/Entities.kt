package de.medando.bloodpressure.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.medando.bloodpressure.domain.model.BodyPosition
import de.medando.bloodpressure.domain.model.MeasurementSite
import de.medando.bloodpressure.domain.model.Reading
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

@Entity(tableName = "reading", indices = [Index("measured_at")])
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "measured_at") val measuredAtEpochMillis: Long,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val site: MeasurementSite?,
    val position: BodyPosition?,
    val device: String?,
    @ColumnInfo(name = "sleep_minutes") val sleepMinutes: Int?,
    val comment: String?,
)

@Entity(tableName = "device")
data class DeviceEntity(
    @PrimaryKey val name: String,
)

fun ReadingEntity.toDomain() = Reading(
    id = id,
    measuredAt = Instant.ofEpochMilli(measuredAtEpochMillis),
    systolic = systolic,
    diastolic = diastolic,
    pulse = pulse,
    site = site,
    position = position,
    device = device,
    sleep = sleepMinutes?.minutes,
    comment = comment,
)

fun Reading.toEntity() = ReadingEntity(
    id = id,
    measuredAtEpochMillis = measuredAt.toEpochMilli(),
    systolic = systolic,
    diastolic = diastolic,
    pulse = pulse,
    site = site,
    position = position,
    device = device?.trim()?.takeIf { it.isNotEmpty() },
    sleepMinutes = sleep?.inWholeMinutes?.toInt()?.takeIf { it > 0 },
    comment = comment?.trim()?.takeIf { it.isNotEmpty() },
)
