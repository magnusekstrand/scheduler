package se.callistaenterprise.scheduler.service;

import org.junit.jupiter.api.Test;
import se.callistaenterprise.scheduler.exception.BadRequestException;
import se.callistaenterprise.scheduler.model.Meeting;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MeetingValidatorTest {

    @Test
    void shouldThrowExceptionWhenMeetingTitleIsNullOrEmpty() {
        Meeting meeting = Meeting.builder()
                .title(null)
                .date(LocalDate.now())
                .start(LocalTime.of(9, 0))
                .end(LocalTime.of(10, 0))
                .build();

        assertThrows(BadRequestException.class, () -> MeetingValidator.validate(meeting));
    }

    @Test
    void shouldThrowExceptionWhenMeetingDateIsNull() {
        Meeting meeting = Meeting.builder()
                .title("Team Meeting")
                .date(null)
                .start(LocalTime.of(9, 0))
                .end(LocalTime.of(10, 0))
                .build();

        assertThrows(BadRequestException.class, () -> MeetingValidator.validate(meeting));
    }

    @Test
    void shouldThrowExceptionWhenMeetingStartTimeIsNull() {
        Meeting meeting = Meeting.builder()
                .title("Team Meeting")
                .date(LocalDate.now())
                .start(null)
                .end(LocalTime.of(10, 0))
                .build();

        assertThrows(BadRequestException.class, () -> MeetingValidator.validate(meeting));
    }

    @Test
    void shouldThrowExceptionWhenMeetingEndTimeIsNull() {
        Meeting meeting = Meeting.builder()
                .title("Team Meeting")
                .date(LocalDate.now())
                .start(LocalTime.of(9, 0))
                .end(null)
                .build();

        assertThrows(BadRequestException.class, () -> MeetingValidator.validate(meeting));
    }

    @Test
    void shouldThrowExceptionWhenStartAndEndTimeAreEqual() {
        LocalTime time = LocalTime.of(9, 0);
        Meeting meeting = Meeting.builder()
                .title("Team Meeting")
                .date(LocalDate.now())
                .start(time)
                .end(time)
                .build();

        assertThrows(BadRequestException.class, () -> MeetingValidator.validate(meeting));
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
        Meeting meeting = Meeting.builder()
                .title("Team Meeting")
                .date(LocalDate.now())
                .start(LocalTime.of(10, 0))
                .end(LocalTime.of(9, 0))
                .build();

        assertThrows(BadRequestException.class, () -> MeetingValidator.validate(meeting));
    }

    @Test
    void shouldThrowExceptionWhenMeetingDurationIsLessThan15Minutes() {
        Meeting meeting = Meeting.builder()
                .title("Team Meeting")
                .date(LocalDate.now())
                .start(LocalTime.of(9, 0))
                .end(LocalTime.of(9, 10))
                .build();

        assertThrows(BadRequestException.class, () -> MeetingValidator.validate(meeting));
    }

    @Test
    void shouldThrowExceptionWhenStartTimeIsBeforeWorkingHours() {
        Meeting meeting = Meeting.builder()
                .title("Team Meeting")
                .date(LocalDate.now())
                .start(LocalTime.of(7, 0))
                .end(LocalTime.of(9, 0))
                .build();

        assertThrows(BadRequestException.class, () -> MeetingValidator.validate(meeting));
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsAfterWorkingHours() {
        Meeting meeting = Meeting.builder()
                .title("Team Meeting")
                .date(LocalDate.now())
                .start(LocalTime.of(17, 0))
                .end(LocalTime.of(18, 0))
                .build();

        assertThrows(BadRequestException.class, () -> MeetingValidator.validate(meeting));
    }
}
