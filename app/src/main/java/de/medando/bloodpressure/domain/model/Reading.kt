package de.medando.bloodpressure.domain.model

import java.time.Instant
import kotlin.time.Duration

data class Reading(
    val id: Long = 0,
    val measuredAt: Instant,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val site: MeasurementSite? = null,
    val position: BodyPosition? = null,
    val device: String? = null,
    val sleep: Duration? = null,
    val comment: String? = null,
) {
    val pulsePressure: Int get() = systolic - diastolic
}

enum class MeasurementSite {
    RIGHT_ARM, LEFT_ARM,
    RIGHT_THIGH, LEFT_THIGH,
    RIGHT_WRIST, LEFT_WRIST,
    RIGHT_ANKLE, LEFT_ANKLE,
    FINGER, TOE, INTRA_ARTERIAL,
}

enum class BodyPosition {
    SITTING, STANDING, RECLINING, LYING, LYING_TILTED_LEFT,
}
