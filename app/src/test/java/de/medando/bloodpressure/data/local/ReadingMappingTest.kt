package de.medando.bloodpressure.data.local

import de.medando.bloodpressure.domain.model.BodyPosition
import de.medando.bloodpressure.domain.model.MeasurementSite
import de.medando.bloodpressure.domain.model.Reading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ReadingMappingTest {

    private val reading = Reading(
        id = 7,
        measuredAt = Instant.parse("2026-11-14T07:30:00Z"),
        systolic = 128,
        diastolic = 82,
        pulse = 64,
        site = MeasurementSite.LEFT_ARM,
        position = BodyPosition.SITTING,
        device = "Omron M500",
        sleep = 7.hours + 30.minutes,
        comment = "after coffee",
    )

    @Test
    fun `round trip keeps all fields`() {
        assertEquals(reading, reading.toEntity().toDomain())
    }

    @Test
    fun `sleep is stored in minutes`() {
        assertEquals(450, reading.toEntity().sleepMinutes)
    }

    @Test
    fun `zero or negative sleep is stored as null`() {
        assertNull(reading.copy(sleep = Duration.ZERO).toEntity().sleepMinutes)
        assertNull(reading.copy(sleep = (-5).minutes).toEntity().sleepMinutes)
    }

    @Test
    fun `blank comment and device are stored as null`() {
        val entity = reading.copy(comment = "  ", device = "").toEntity()
        assertNull(entity.comment)
        assertNull(entity.device)
    }

    @Test
    fun `comment and device are trimmed`() {
        val entity = reading.copy(comment = " dizzy\n", device = " Omron ").toEntity()
        assertEquals("dizzy", entity.comment)
        assertEquals("Omron", entity.device)
    }

    @Test
    fun `pulse pressure is systolic minus diastolic`() {
        assertEquals(46, reading.pulsePressure)
    }
}
