package se.callistaenterprise.scheduler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import se.callistaenterprise.scheduler.datasource.MeetingStorage;
import se.callistaenterprise.scheduler.entity.Meeting;
import se.callistaenterprise.scheduler.model.Either;
import se.callistaenterprise.scheduler.validation.SchedulerErrors;
import se.callistaenterprise.scheduler.validation.validators.MeetingValidator;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static se.callistaenterprise.scheduler.model.Either.left;
import static se.callistaenterprise.scheduler.model.Either.right;
import static se.callistaenterprise.scheduler.validation.SchedulerErrors.ErrorCode.TIME_NOT_AVAILABLE;
import static se.callistaenterprise.scheduler.validation.Validator.validate;

@Slf4j
@Service
public class MeetingService {

    public static final String WORKING_HOURS_START = "09:00";
    public static final String WORKING_HOURS_END = "17:00";

    public static final Comparator<Meeting> dateComparator = (o1, o2) -> o1.getDate().compareTo(o2.getDate());
    public static final Comparator<Meeting> timeComparator = (o1, o2) -> o1.getStart().compareTo(o2.getStart());

    public List<Meeting> getMeetings() {
        return MeetingStorage.findAll().stream().sorted(dateComparator.thenComparing(timeComparator)).toList();
    }

    public Either<Meeting, Errors> getMeeting(Long id) {
        if (id == null) {
            return right(SchedulerErrors.createErrors(id, "id", SchedulerErrors.ErrorCode.FIELD_INVALID, "id cannot be null"));
        }

        Meeting meeting = MeetingStorage.find(id);
        if (meeting == null) {
            return right(SchedulerErrors.createErrors(id, "id", SchedulerErrors.ErrorCode.RESOURCE_NOT_FOUND, "Cannot find meeting with id = " + id));
        }

        return left(meeting);
    }

    public Either<Meeting, Errors> addMeeting(Meeting meeting) {
        Errors errors = validate(meeting, new MeetingValidator());
        if (errors.hasErrors()) {
            return right(errors);
        }

        if (isTimeAvailable(meeting)) {
            return left(MeetingStorage.insert(meeting));
        }

        errors.reject(TIME_NOT_AVAILABLE.name());
        return right(errors);
    }

    public List<Meeting> addMeeting(LocalDate date, Long meetingTimeInMinutes) {
        List<Meeting> existingMeetings = MeetingStorage.findAll().stream()
            .filter(item -> item.getDate().equals(date))
            .sorted(timeComparator)
            .toList();

        LocalTime startOfDay = LocalTime.parse(WORKING_HOURS_START);
        LocalTime endOfDay = LocalTime.parse(WORKING_HOURS_END);

        Meeting startBoundary = Meeting.builder().title("startBoundary").date(date).start(startOfDay.minusMinutes(15L)).end(startOfDay).build();
        Meeting endBoundary = Meeting.builder().title("endBoundary").date(date).start(endOfDay).end(endOfDay.plusMinutes(15L)).build();

        List<Meeting> boundaryList = new ArrayList<>();
        boundaryList.addFirst(startBoundary);
        boundaryList.addAll(existingMeetings);
        boundaryList.addLast(endBoundary);

        List<Meeting> availableSlots = new ArrayList<>();

        int ctr = 0;
        while (ctr < boundaryList.size() - 1) {
            Meeting m1 = boundaryList.get(ctr);
            Meeting m2 = boundaryList.get(ctr + 1);

            long differenceBetweenMeetings = Duration.between(m1.getEnd(), m2.getStart()).toMinutes();
            if (differenceBetweenMeetings > meetingTimeInMinutes) {
                Meeting m = Meeting.builder().date(date).start(m1.getEnd()).end(m2.getStart()).build();
                availableSlots.add(m);
            }

            ctr++;
        }

        return availableSlots;
    }

    private boolean isTimeAvailable(Meeting meeting) {
        return !isTimeConflicting(meeting) && isMeetingTimeDurationWithinLimits(meeting);
    }

    private boolean isTimeConflicting(Meeting meeting) {
        // Narrow list to same day
        List<Meeting> existingMeetings = MeetingStorage.findAll().stream()
            .filter(item -> item.getDate().equals(meeting.getDate()))
            .toList();

        if (existingMeetings.isEmpty()) {
            return false;
        }

        return existingMeetings.stream()
            .filter(item -> isBetween(meeting.getStart(), item.getStart(), item.getEnd()))
            .anyMatch(item -> isBetween(meeting.getEnd(), item.getStart(), item.getEnd()));
    }

    private boolean isMeetingTimeDurationWithinLimits(Meeting meeting) {
        // Narrow list to same day and sort meetings by start time
        List<Meeting> existingMeetings = MeetingStorage.findAll().stream()
            .filter(item -> item.getDate().equals(meeting.getDate()))
            .sorted(timeComparator)
            .toList();

        if (existingMeetings.isEmpty()) {
            return true;
        }

        LocalTime startOfDay = LocalTime.parse(WORKING_HOURS_START);
        LocalTime endOfDay = LocalTime.parse(WORKING_HOURS_END);
        LocalTime startTime = meeting.getStart();
        LocalTime endTime = meeting.getEnd();

        long meetingTimeInMinutes = Duration.between(startTime, endTime).toMinutes();

        Meeting startBoundary = Meeting.builder().title("startBoundary").date(meeting.getDate()).start(startOfDay.minusMinutes(15L)).end(startOfDay).build();
        Meeting endBoundary = Meeting.builder().title("endBoundary").date(meeting.getDate()).start(endOfDay).end(endOfDay.plusMinutes(15L)).build();

        List<Meeting> boundaryList = new ArrayList<>();
        boundaryList.addFirst(startBoundary);
        boundaryList.addAll(existingMeetings);
        boundaryList.addLast(endBoundary);

        int ctr = 0;
        while (ctr < boundaryList.size() - 1) {
            Meeting m1 = boundaryList.get(ctr);
            Meeting m2 = boundaryList.get(ctr + 1);

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
