package de.medando.bloodpressure.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class Settings(
    /** `null` follows the device locale. */
    val firstDayOfWeek: DayOfWeek? = null,
    val timesOfDay: TimesOfDay = TimesOfDay(),
    val guideline: Guideline = Guideline.ESC_2024,
    /** `null` uses the guideline's default target. */
    val targetRange: TargetRange? = null,
    val name: String? = null,
    val dateOfBirth: LocalDate? = null,
)

data class TimesOfDay(
    val morning: LocalTime = LocalTime.of(5, 0),
    val afternoon: LocalTime = LocalTime.of(11, 0),
    val evening: LocalTime = LocalTime.of(17, 0),
    val night: LocalTime = LocalTime.of(23, 0),
)

data class TargetRange(val maxSystolic: Int, val maxDiastolic: Int)

enum class Guideline { ESC_2024, ESH_2023, ACC_AHA_2017 }
