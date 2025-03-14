package se.callistaenterprise.scheduler.model

import java.time.LocalDate
import java.time.LocalTime

data class Meeting(
    val id: Long?,
    val title: String,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
)
