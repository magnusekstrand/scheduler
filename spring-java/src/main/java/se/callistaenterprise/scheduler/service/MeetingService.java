package se.callistaenterprise.scheduler.service;

import static se.callistaenterprise.scheduler.model.Either.left;
import static se.callistaenterprise.scheduler.model.Either.right;
import static se.callistaenterprise.scheduler.validation.SchedulerErrors.ErrorCode.FIELD_INVALID;
import static se.callistaenterprise.scheduler.validation.SchedulerErrors.ErrorCode.INSERT_FAILED;
import static se.callistaenterprise.scheduler.validation.SchedulerErrors.ErrorCode.RESOURCE_NOT_FOUND;
import static se.callistaenterprise.scheduler.validation.SchedulerErrors.ErrorCode.TIME_NOT_AVAILABLE;
import static se.callistaenterprise.scheduler.validation.Validator.validate;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import se.callistaenterprise.scheduler.config.SchedulerProperties;
import se.callistaenterprise.scheduler.datasource.MeetingStorage;
import se.callistaenterprise.scheduler.entity.Meeting;
import se.callistaenterprise.scheduler.model.Either;
import se.callistaenterprise.scheduler.validation.SchedulerErrors;
import se.callistaenterprise.scheduler.validation.validators.MeetingValidator;

@Slf4j
@Service
public class MeetingService {

  private static final long BOUNDARY_TIME_BUFFER = 15L; // Minutes

  private final SchedulerProperties schedulerProperties;
  private final MeetingStorage meetingStorage;

  public MeetingService(SchedulerProperties schedulerProperties, MeetingStorage meetingStorage) {
    this.schedulerProperties = schedulerProperties;
    this.meetingStorage = meetingStorage;
  }

  /*
   Returns a list of all meetings sorted by date and start time
  */
  public List<Meeting> getMeetings() {
    return meetingStorage.sort().stream().toList();
  }

  public Either<Meeting, Errors> getMeeting(Long id) {
    if (id == null) {
      return right(SchedulerErrors.createErrors(id, "id", FIELD_INVALID, "id cannot be null"));
    }

    Meeting meeting = meetingStorage.getById(id);
    return Optional.ofNullable(meeting)
        .map(Either::left)
        .orElseGet(
            () ->
                right(
                    SchedulerErrors.createErrors(
                        id, "id", RESOURCE_NOT_FOUND, "Cannot find meeting with id = " + id)));
  }

  public Either<Meeting, Errors> addMeeting(Meeting meeting) {
    Errors errors = validate(meeting, new MeetingValidator(schedulerProperties));
    if (errors.hasErrors()) {
      return right(errors);
    }

    if (isTimeAvailable(meeting)) {
      Meeting savedMeeting = meetingStorage.add(meeting);
      return savedMeeting != null
          ? left(savedMeeting)
          : right(SchedulerErrors.createErrors(meeting, null, INSERT_FAILED, "Cannot add meeting"));
    }

    errors.reject(TIME_NOT_AVAILABLE.name());
    return right(errors);
  }

  public List<Meeting> addMeeting(LocalDate date, Long meetingTimeInMinutes) {
    LocalTime startOfDay = getStartOfDay();
    LocalTime endOfDay = getEndOfDay();

    List<Meeting> boundaryList =
        new ArrayList<>(
            meetingStorage.sort().stream().filter(item -> item.getDate().equals(date)).toList());

    // Add boundaries to list
    boundaryList.addFirst(
        createBoundaryMeeting(
            "startBoundary", date, startOfDay.minusMinutes(BOUNDARY_TIME_BUFFER), startOfDay));
    boundaryList.add(
        createBoundaryMeeting(
            "endBoundary", date, endOfDay, endOfDay.plusMinutes(BOUNDARY_TIME_BUFFER)));

    return IntStream.range(0, boundaryList.size() - 1)
        .mapToObj(
            i -> {
              Meeting m1 = boundaryList.get(i);
              Meeting m2 = boundaryList.get(i + 1);
              long differenceBetweenMeetings =
                  Duration.between(m1.getEnd(), m2.getStart()).toMinutes();
              return differenceBetweenMeetings > meetingTimeInMinutes
                  ? Meeting.builder().date(date).start(m1.getEnd()).end(m2.getStart()).build()
                  : null;
            })
        .filter(Objects::nonNull)
        .toList();
  }

  private Meeting createBoundaryMeeting(
      String title, LocalDate date, LocalTime start, LocalTime end) {
    return Meeting.builder().title(title).date(date).start(start).end(end).build();
  }

  private LocalTime getStartOfDay() {
    return schedulerProperties.getWorkingHours().getStart();
  }

  private LocalTime getEndOfDay() {
    return schedulerProperties.getWorkingHours().getEnd();
  }

  private boolean isTimeAvailable(Meeting meeting) {
    return isWorkingDay(meeting.getDate())
        && !isTimeConflicting(meeting)
        && isMeetingDurationValid(meeting);
  }

  private boolean isTimeConflicting(Meeting meeting) {
    // Narrow list to same day
    List<Meeting> existingMeetings =
        meetingStorage.sort().stream()
            .filter(item -> item.getDate().equals(meeting.getDate()))
            .toList();

    if (existingMeetings.isEmpty()) {
      return false;
    }

    return existingMeetings.stream()
        .anyMatch(
            item ->
                isTimeBetween(meeting.getStart(), item.getStart(), item.getEnd())
                    || isTimeBetween(meeting.getEnd(), item.getStart(), item.getEnd()));
  }

  private boolean isMeetingDurationValid(Meeting meeting) {
    List<Meeting> existingMeetings =
        meetingStorage.sort().stream()
            .filter(item -> item.getDate().equals(meeting.getDate()))
            .toList();

    if (existingMeetings.isEmpty()) {
      return true;
    }

    LocalTime startOfDay = getStartOfDay();
    LocalTime endOfDay = getEndOfDay();
    long meetingDuration = Duration.between(meeting.getStart(), meeting.getEnd()).toMinutes();

    Meeting startBoundaryMeeting =
        createBoundaryMeeting(
            "startBoundary",
            meeting.getDate(),
            startOfDay.minusMinutes(BOUNDARY_TIME_BUFFER),
            startOfDay);
    Meeting endBoundaryMeeting =
        createBoundaryMeeting(
            "endBoundary", meeting.getDate(), endOfDay, endOfDay.plusMinutes(BOUNDARY_TIME_BUFFER));

    List<Meeting> allMeetings =
        Stream.concat(
                Stream.concat(Stream.of(startBoundaryMeeting), existingMeetings.stream()),
                Stream.of(endBoundaryMeeting))
            .toList();

    return IntStream.range(0, allMeetings.size() - 1)
        .mapToObj(index -> Map.entry(allMeetings.get(index), allMeetings.get(index + 1)))
        .filter(
            pair ->
                isTimeBetween(
                    meeting.getStart(), pair.getKey().getEnd(), pair.getValue().getStart()))
        .map(
            pair ->
                Duration.between(pair.getKey().getEnd(), pair.getValue().getStart()).toMinutes())
        .anyMatch(gapDuration -> gapDuration > meetingDuration);
  }

  private boolean isTimeBetween(LocalTime time, LocalTime start, LocalTime end) {
    return !time.isBefore(start) && !time.isAfter(end);
  }

  private boolean isWorkingDay(LocalDate date) {
    DayOfWeek dayOfWeek = date.getDayOfWeek();
    return !schedulerProperties.getWeekends().contains(dayOfWeek.name());
  }
}
