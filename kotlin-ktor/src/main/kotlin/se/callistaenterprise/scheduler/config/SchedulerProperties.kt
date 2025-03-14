package se.callistaenterprise.scheduler.config

import java.time.LocalTime

enum class Days(val isWeekend: Boolean = false) {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,

    // Default values overridden
    SATURDAY(true),
    SUNDAY(true),
    ;

    companion object {
        fun isWeekend(obj: Days): Boolean {
            return obj.name.compareTo("SATURDAY") == 0 || obj.name.compareTo("SUNDAY") == 0
        }
    }
}

data class WorkingHours(val start: String, val end: String) {
    val startOfDay: LocalTime
        get() = LocalTime.parse(start)

    val endOfDay: LocalTime
        get() = LocalTime.parse(end)
}
