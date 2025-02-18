package se.callistaenterprise.scheduler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.callistaenterprise.scheduler.datasource.MeetingStorage;
import se.callistaenterprise.scheduler.exception.BadRequestException;
import se.callistaenterprise.scheduler.model.Meeting;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import static se.callistaenterprise.scheduler.service.MeetingValidator.validate;

@Slf4j
@Service
public class MeetingService {

    public static final String WORKING_HOURS_START = "09:00";
    public static final String WORKING_HOURS_END = "17:00";

    public List<Meeting> getMeetings() {
        return MeetingStorage.findAll();
    }

    public Meeting getMeeting(Long id) {
        if (id == null) {
            throw new BadRequestException("id cannot be null");
        }

        Meeting meeting = MeetingStorage.find(id);
        if (meeting == null) {
            log.info("Cannot find meeting with id = {}", id);
        }
        return meeting;
    }

    public Meeting addMeeting(Meeting meeting) throws BadRequestException {
        validate(meeting);

        if (isTimeAvailable(meeting)) {
            return MeetingStorage.insert(meeting);
        }

        throw new BadRequestException("Meeting time is not available");
    }

    boolean isTimeAvailable(Meeting meeting) {
        // Narrow list to same day
        List<Meeting> meetings = MeetingStorage.findAll().stream()
            .filter(item -> item.getDate().equals(meeting.getDate()))
            .toList();

        if (meetings.isEmpty()) {
            return true;
        }

        if (isTimeConflicting(meeting, meetings)) {
            return false;
        }

        return isMeetingTimeDurationWithinLimits(meeting, meetings);
    }

    boolean isTimeConflicting(Meeting meeting, List<Meeting> existingMeetings) {
        if (existingMeetings == null || existingMeetings.isEmpty()) {
            return false;
        }

        return existingMeetings.stream()
            .filter(item -> item.getDate().equals(meeting.getDate()))
            .filter(item -> isBetween(meeting.getStart(), item.getStart(), item.getEnd()))
            .anyMatch(item -> isBetween(meeting.getEnd(), item.getStart(), item.getEnd()));
    }

    boolean isMeetingTimeDurationWithinLimits(Meeting meeting, List<Meeting> existingMeetings) {
        if (existingMeetings == null || existingMeetings.isEmpty()) {
            return true;
        }

        LocalTime startOfDay = LocalTime.parse(WORKING_HOURS_START);
        LocalTime endOfDay = LocalTime.parse(WORKING_HOURS_END);
        LocalTime startTime = meeting.getStart();
        LocalTime endTime = meeting.getEnd();

        long meetingTimeInMinutes = Duration.between(startTime, endTime).toMinutes();

        if (existingMeetings.size() == 1) {
            if (startTime.isBefore(existingMeetings.getFirst().getStart())) {
                long diff = Duration.between(startOfDay, existingMeetings.getFirst().getStart()).toMinutes();
                return diff > meetingTimeInMinutes;
            }

            if (startTime.isAfter(existingMeetings.getLast().getEnd())) {
                long diff = Duration.between(existingMeetings.getLast().getEnd(), endOfDay).toMinutes();
                return diff > meetingTimeInMinutes;
            }

            return false;
        }

        int ctr = 0;
        while (ctr < existingMeetings.size()) {
            Meeting m1 = existingMeetings.get(ctr);
            Meeting m2 = existingMeetings.get(ctr + 1);

            if (isBetween(startTime, m1.getEnd(), m2.getStart()) ) {
                long diff = Duration.between(m1.getEnd(), m2.getStart()).toMinutes();
                return diff > meetingTimeInMinutes;
            }

            ctr++;
        }

        return false;
    }

    private boolean isBetween(LocalTime targetTime, LocalTime startTime, LocalTime endTime) {
        return !targetTime.isBefore(startTime) && !targetTime.isAfter(endTime);
    }


}
