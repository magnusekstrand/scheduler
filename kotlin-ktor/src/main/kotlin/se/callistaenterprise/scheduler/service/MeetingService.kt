package se.callistaenterprise.scheduler.service

import se.callistaenterprise.scheduler.config.Days
import se.callistaenterprise.scheduler.config.WorkingHours
import se.callistaenterprise.scheduler.model.Meeting
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class MeetingService(private val repository: MeetingRepository) {
    companion object {
        private const val BOUNDARY_TIME_BUFFER_MINUTES = 30L
    }

    private val workingHours = WorkingHours("08:00", "17:00")

    suspend fun add(meeting: Meeting): Meeting? {
        return if (isTimeAvailable(meeting)) {
            repository.create(meeting).let { repository.read(it) }
        } else {
            null
        }
    }

    suspend fun all(): List<Meeting> = repository.readAll()

    suspend fun findById(id: Long): Meeting? {
        return runCatching { repository.read(id) }.getOrNull()
    }

    suspend fun findIntervals(
        date: LocalDate,
        meetingDurationMinutes: Long,
    ): List<Meeting> {
        val meetingsWithBoundaries = addWorkingHourBoundaries(repository.read(date), date)
        return calculateMeetingGapDurations(meetingsWithBoundaries)
            .mapIndexedNotNull { index, gapDuration ->
                if (gapDuration > meetingDurationMinutes) {
                    val endOfCurrent = meetingsWithBoundaries[index].end
                    val startOfNext = meetingsWithBoundaries[index + 1].start
                    Meeting(null, "", date, endOfCurrent, startOfNext)
                } else {
                    null
                }
            }
    }

    suspend fun update(
        id: Long,
        meeting: Meeting,
    ) = repository.update(id, meeting)

    suspend fun delete(id: Long) = repository.delete(id)

    private fun addWorkingHourBoundaries(
        existingMeetings: List<Meeting>,
        date: LocalDate,
    ): List<Meeting> {
        val startBoundary =
            createBoundaryMeeting(
                "startBoundary",
                date,
                workingHours.startOfDay.minusMinutes(BOUNDARY_TIME_BUFFER_MINUTES),
                workingHours.startOfDay,
            )
        val endBoundary =
            createBoundaryMeeting(
                "endBoundary",
                date,
                workingHours.endOfDay,
                workingHours.endOfDay.plusMinutes(BOUNDARY_TIME_BUFFER_MINUTES),
            )
        return listOf(startBoundary) + existingMeetings + endBoundary
    }

    private fun createBoundaryMeeting(
        title: String,
        date: LocalDate,
        start: LocalTime,
        end: LocalTime,
    ) = Meeting(null, title, date, start, end)

    private fun calculateMeetingGapDurations(meetings: List<Meeting>): List<Long> {
        return meetings.zipWithNext { current, next ->
            Duration.between(current.end, next.start).toMinutes()
        }
    }

    private suspend fun isTimeAvailable(meeting: Meeting): Boolean {
        return isWorkingDay(meeting.date) &&
            !hasConflictingMeetings(meeting) &&
            isMeetingDurationValid(meeting)
    }

    private suspend fun hasConflictingMeetings(meeting: Meeting): Boolean {
        val sameDayMeetings = repository.read(meeting.date)
        return sameDayMeetings.any { isOverlapping(meeting, it) }
    }

    private fun isOverlapping(
        meeting1: Meeting,
        meeting2: Meeting,
    ): Boolean {
        return isTimeBetween(meeting1.start, meeting2.start, meeting2.end) ||
            isTimeBetween(meeting1.end, meeting2.start, meeting2.end)
    }

    private suspend fun isMeetingDurationValid(meeting: Meeting): Boolean {
        val allMeetingsWithBoundaries = addWorkingHourBoundaries(repository.read(meeting.date), meeting.date)
        val meetingDuration = Duration.between(meeting.start, meeting.end).toMinutes()
        return calculateMeetingGapDurations(allMeetingsWithBoundaries).any { it > meetingDuration }
    }

    private fun isTimeBetween(
        time: LocalTime,
        start: LocalTime,
        end: LocalTime,
    ): Boolean {
        return !time.isBefore(start) && !time.isAfter(end)
    }

    private fun isWorkingDay(date: LocalDate): Boolean {
        return !Days.isWeekend(Days.valueOf(date.dayOfWeek.name))
    }
}
