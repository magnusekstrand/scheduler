package se.callistaenterprise.scheduler.service;

import static se.callistaenterprise.scheduler.model.Either.left;
import static se.callistaenterprise.scheduler.model.Either.right;
import static se.callistaenterprise.scheduler.validation.SchedulerErrors.ErrorCode.TIME_NOT_AVAILABLE;
import static se.callistaenterprise.scheduler.validation.Validator.validate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import se.callistaenterprise.scheduler.config.WorkingHours;
import se.callistaenterprise.scheduler.datasource.MeetingStorage;
import se.callistaenterprise.scheduler.entity.Meeting;
import se.callistaenterprise.scheduler.model.Either;
import se.callistaenterprise.scheduler.validation.SchedulerErrors;
import se.callistaenterprise.scheduler.validation.validators.MeetingValidator;

@Slf4j
@Service
public class MeetingService {

  public static final Comparator<Meeting> dateComparator =
      (o1, o2) -> o1.getDate().compareTo(o2.getDate());
  public static final Comparator<Meeting> timeComparator =
      (o1, o2) -> o1.getStart().compareTo(o2.getStart());

  private static final long BOUNDARY_TIME_BUFFER = 15L; // Minutes

  private final WorkingHours workingHours;

  public MeetingService(WorkingHours workingHours) {
    this.workingHours = workingHours;
  }

  public List<Meeting> getMeetings() {
    return MeetingStorage.findAll().stream()
        .sorted(dateComparator.thenComparing(timeComparator))
        .toList();
  }

  public Either<Meeting, Errors> getMeeting(Long id) {
    if (id == null) {
      return right(
          SchedulerErrors.createErrors(
              id, "id", SchedulerErrors.ErrorCode.FIELD_INVALID, "id cannot be null"));
    }

    Meeting meeting = MeetingStorage.find(id);
    if (meeting == null) {
      return right(
          SchedulerErrors.createErrors(
              id,
              "id",
              SchedulerErrors.ErrorCode.RESOURCE_NOT_FOUND,
              "Cannot find meeting with id = " + id));
    }

    return left(meeting);
  }

  public Either<Meeting, Errors> addMeeting(Meeting meeting) {
    Errors errors = validate(meeting, new MeetingValidator(workingHours));
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
    LocalTime startOfDay = workingHours.getStart();
    LocalTime endOfDay = workingHours.getEnd();

    List<Meeting> boundaryList =
        new ArrayList<>(
            MeetingStorage.findAll().stream()
                .filter(item -> item.getDate().equals(date))
                .sorted(timeComparator)
                .toList());

    // Add boundaries to list
    boundaryList.addFirst(
        createBoundaryMeeting("startBoundary", date, startOfDay.minusMinutes(15L), startOfDay));
    boundaryList.add(
        createBoundaryMeeting("endBoundary", date, endOfDay, endOfDay.plusMinutes(15L)));

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

  private boolean isTimeAvailable(Meeting meeting) {
    return !isTimeConflicting(meeting) && isMeetingDurationValid(meeting);
  }

  private boolean isTimeConflicting(Meeting meeting) {
    // Narrow list to same day
    List<Meeting> existingMeetings =
        MeetingStorage.findAll().stream()
            .filter(item -> item.getDate().equals(meeting.getDate()))
            .toList();

    if (existingMeetings.isEmpty()) {
      return false;
    }

    return existingMeetings.stream()
        .filter(item -> isTimeBetween(meeting.getStart(), item.getStart(), item.getEnd()))
        .anyMatch(item -> isTimeBetween(meeting.getEnd(), item.getStart(), item.getEnd()));
  }

  private boolean isMeetingDurationValid(Meeting meeting) {
    List<Meeting> existingMeetings =
        MeetingStorage.findAll().stream()
            .filter(item -> item.getDate().equals(meeting.getDate()))
            .sorted(timeComparator)
            .toList();

    if (existingMeetings.isEmpty()) {
      return true;
    }

    LocalTime startOfDay = workingHours.getStart();
    LocalTime endOfDay = workingHours.getEnd();
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

  private Meeting createBoundaryMeeting(
      String title, LocalDate date, LocalTime start, LocalTime end) {
    return Meeting.builder().title(title).date(date).start(start).end(end).build();
  }

  private boolean isTimeBetween(LocalTime time, LocalTime start, LocalTime end) {
    return !time.isBefore(start) && !time.isAfter(end);
  }
}
