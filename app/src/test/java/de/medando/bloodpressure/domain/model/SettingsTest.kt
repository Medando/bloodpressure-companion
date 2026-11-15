package de.medando.bloodpressure.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.util.Locale

class SettingsTest {

    @Test
    fun `system first day of week without fw extension is the region's default`() {
        assertEquals(DayOfWeek.MONDAY, systemFirstDayOfWeek(Locale.forLanguageTag("de-DE")))
        assertEquals(DayOfWeek.SUNDAY, systemFirstDayOfWeek(Locale.forLanguageTag("en-US")))
        assertEquals(DayOfWeek.SATURDAY, systemFirstDayOfWeek(Locale.forLanguageTag("ar-EG")))
    }

    @Test
    fun `system first day of week follows the fw extension of regional preferences`() {
        mapOf(
            "mon" to DayOfWeek.MONDAY,
            "sat" to DayOfWeek.SATURDAY,
            "sun" to DayOfWeek.SUNDAY,
            "fri" to DayOfWeek.FRIDAY,
        ).forEach { (fw, day) ->
            assertEquals(fw, day, systemFirstDayOfWeek(Locale.forLanguageTag("de-DE-u-fw-$fw")))
        }
    }

    @Test
    fun `fw values map to their day, anything else to none`() {
        assertEquals(DayOfWeek.entries, listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun").map(::dayOfFw))
        assertEquals(DayOfWeek.MONDAY, dayOfFw("MON"))
        listOf("", "m", "monday", "xyz").forEach { assertNull(it, dayOfFw(it)) }
    }

    @Test
    fun `unknown fw value falls back to the region's default`() {
        assertEquals(DayOfWeek.SUNDAY, systemFirstDayOfWeek(Locale.forLanguageTag("en-US-u-fw-xyz")))
        assertEquals(DayOfWeek.SUNDAY, systemFirstDayOfWeek(Locale.forLanguageTag("en-US-u-fw-monday")))
        assertEquals(DayOfWeek.MONDAY, systemFirstDayOfWeek(Locale.forLanguageTag("de-DE-u-fw-xyz")))
    }
}
