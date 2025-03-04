package se.callistaenterprise.scheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.Errors;
import se.callistaenterprise.scheduler.config.SchedulerProperties;
import se.callistaenterprise.scheduler.datasource.MeetingStorage;
import se.callistaenterprise.scheduler.entity.Meeting;
import se.callistaenterprise.scheduler.model.Either;

@SpringBootTest
class MeetingServiceTest {

  @Autowired private MeetingService meetingService;

  @MockitoBean private SchedulerProperties schedulerProperties;

  @MockitoBean private MeetingStorage meetingStorage;

  @Test
  void testAddMeeting_Success() {
    when(meetingStorage.add(any(Meeting.class)))
        .thenReturn(
            Meeting.builder()
                .id(1L)
                .title("Team Meeting")
                .date(LocalDate.of(2023, 12, 1))
                .start(LocalTime.of(10, 0))
                .end(LocalTime.of(11, 0))
                .build());

    when(schedulerProperties.getWeekends()).thenReturn(Collections.emptyList());
    SchedulerProperties.WorkingHours workingHours =
        new SchedulerProperties.WorkingHours("08:00", "18:00");
    when(schedulerProperties.getWorkingHours()).thenReturn(workingHours);

    Meeting meeting =
        Meeting.builder()
            .id(null)
            .title("Team Meeting")
            .date(LocalDate.of(2023, 12, 1))
            .start(LocalTime.of(10, 0))
            .end(LocalTime.of(11, 0))
            .build();

    Either<Meeting, Errors> result = meetingService.addMeeting(meeting);

    assertThat(result.hasErrors()).isFalse();
    assertThat(result.getLeft()).isNotNull();
    assertThat(result.getLeft().getTitle()).isEqualTo("Team Meeting");
  }

  @Test
  void testAddMeeting_TimeConflict() {
    Meeting conflictingMeeting =
        Meeting.builder()
            .id(1L)
            .title("Existing Meeting")
            .date(LocalDate.of(2023, 12, 1))
            .start(LocalTime.of(10, 0))
            .end(LocalTime.of(11, 0))
            .build();

    when(meetingStorage.sort()).thenReturn(Collections.singletonList(conflictingMeeting));

    when(schedulerProperties.getWeekends()).thenReturn(Collections.emptyList());
    SchedulerProperties.WorkingHours workingHours =
        new SchedulerProperties.WorkingHours("08:00", "18:00");
    when(schedulerProperties.getWorkingHours()).thenReturn(workingHours);

    Meeting newMeeting =
        Meeting.builder()
            .id(null)
            .title("Team Meeting")
            .date(LocalDate.of(2023, 12, 1))
            .start(LocalTime.of(10, 30))
            .end(LocalTime.of(11, 30))
            .build();

    Either<Meeting, Errors> result = meetingService.addMeeting(newMeeting);

    assertThat(result.hasErrors()).isTrue();
    assertThat(result.getLeft()).isNull();
    assertThat(result.getRight()).isNotNull();
  }

  @Test
  void testAddMeeting_InvalidData() {
    when(schedulerProperties.getWeekends()).thenReturn(Collections.emptyList());
    SchedulerProperties.WorkingHours workingHours =
        new SchedulerProperties.WorkingHours("08:00", "18:00");
    when(schedulerProperties.getWorkingHours()).thenReturn(workingHours);

    Meeting invalidMeeting =
        Meeting.builder()
            .id(null)
            .title("")
            .date(LocalDate.of(2023, 12, 1))
            .start(LocalTime.of(10, 0))
            .end(LocalTime.of(9, 0)) // Invalid time range
            .build();

    Either<Meeting, Errors> result = meetingService.addMeeting(invalidMeeting);

    assertThat(result.hasErrors()).isTrue();
    assertThat(result.getLeft()).isNull();
    assertThat(result.getRight()).isNotNull();
    assertThat(result.hasErrors()).isTrue();
    assertThat(result.getLeft()).isNull();
    assertThat(result.getRight()).isNotNull();
    assertThat(result.hasErrors()).isTrue();
    assertThat(result.getLeft()).isNull();
    assertThat(result.getRight()).isNotNull();
    assertThat(result.hasErrors()).isTrue();
    assertThat(result.getLeft()).isNull();
    assertThat(result.getRight()).isNotNull();
  }

  @Test
  void testAddMeeting_OutOfWorkingHours() {
    when(schedulerProperties.getWeekends()).thenReturn(Collections.emptyList());
    SchedulerProperties.WorkingHours workingHours =
        new SchedulerProperties.WorkingHours("08:00", "18:00");
    when(schedulerProperties.getWorkingHours()).thenReturn(workingHours);

    Meeting outOfHoursMeeting =
        Meeting.builder()
            .id(null)
            .title("Late Meeting")
            .date(LocalDate.of(2023, 12, 1))
            .start(LocalTime.of(19, 0))
            .end(LocalTime.of(20, 0))
            .build();

    Either<Meeting, Errors> result = meetingService.addMeeting(outOfHoursMeeting);

    assertThat(result.hasErrors()).isTrue();
    assertThat(result.getLeft()).isNull();
    assertThat(result.getRight()).isNotNull();
    assertThat(result.hasErrors()).isTrue();
    assertThat(result.getLeft()).isNull();
    assertThat(result.getRight()).isNotNull();
    assertThat(result.hasErrors()).isTrue();
    assertThat(result.getLeft()).isNull();
    assertThat(result.getRight()).isNotNull();
  }

  @Test
  void testAddMeeting_NullMeeting() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> meetingService.addMeeting(null));
  }
}
