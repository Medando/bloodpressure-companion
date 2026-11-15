package de.medando.bloodpressure.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.util.Locale

class DayDisplayNameTest {

    @Test
    fun `day name is the locale's full name with a capital first letter`() {
        assertEquals("Lundi", DayOfWeek.MONDAY.displayName(Locale.FRANCE))
        assertEquals("Sonntag", DayOfWeek.SUNDAY.displayName(Locale.GERMANY))
        assertEquals("Saturday", DayOfWeek.SATURDAY.displayName(Locale.US))
    }
}
