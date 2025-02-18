package se.callistaenterprise.scheduler.service;

import org.springframework.util.StringUtils;
import se.callistaenterprise.scheduler.exception.BadRequestException;
import se.callistaenterprise.scheduler.model.Meeting;

import java.time.Duration;
import java.time.LocalTime;

import static se.callistaenterprise.scheduler.service.MeetingService.WORKING_HOURS_END;
import static se.callistaenterprise.scheduler.service.MeetingService.WORKING_HOURS_START;

public class MeetingValidator {

    static void validate(Meeting meeting) throws BadRequestException {
        // Require non null values
        if (!StringUtils.hasText(meeting.getTitle())) {
            throw new BadRequestException("Meeting must have a title");
        }
        if (meeting.getDate() == null) {
            throw new BadRequestException("Meeting must have a date");
        }
        if (meeting.getStart() == null) {
            throw new BadRequestException("Meeting must have a start time");
        }
        if (meeting.getEnd() == null) {
            throw new BadRequestException("Meeting must have an end time");
        }

        // Check valid start and end times
        long differenceInMinutes = Duration.between(meeting.getStart(), meeting.getEnd()).toMinutes();
        if (differenceInMinutes == 0) {
            throw new BadRequestException("Meeting start and end time cannot be equal");
        }
        if (differenceInMinutes < 0) {
            throw new BadRequestException("Meeting end time must be after start time");
        }
        if (differenceInMinutes < 15) {
            throw new BadRequestException("Meeting time cannot be less than 15 minutes");
        }

        // Check meeting is within working hours
        if (meeting.getStart().isBefore(LocalTime.parse(WORKING_HOURS_START))) {
            throw new BadRequestException("Meeting start time cannot be before " + WORKING_HOURS_START);
        }
        if (meeting.getEnd().isAfter(LocalTime.parse(WORKING_HOURS_END))) {
            throw new BadRequestException("Meeting end time cannot be after " + WORKING_HOURS_END);
        }
    }
}
