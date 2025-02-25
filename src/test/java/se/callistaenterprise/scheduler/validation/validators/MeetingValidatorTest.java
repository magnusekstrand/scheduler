package se.callistaenterprise.scheduler.validation.validators;

import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.SimpleErrors;
import se.callistaenterprise.scheduler.entity.Meeting;
import se.callistaenterprise.scheduler.validation.SchedulerErrors;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static se.callistaenterprise.scheduler.service.MeetingService.WORKING_HOURS_END;
import static se.callistaenterprise.scheduler.service.MeetingService.WORKING_HOURS_START;
import static se.callistaenterprise.scheduler.validation.SchedulerErrors.ErrorCode.FIELD_INVALID;
import static se.callistaenterprise.scheduler.validation.SchedulerErrors.ErrorCode.FIELD_REQUIRED;

class MeetingValidatorTest {

    MeetingValidator validator = new MeetingValidator();
    Errors errors;

    @Test
    void validate_TitleIsEmpty_ShouldRejectTitle() {
        Meeting meeting = Meeting.builder()
            .title(null)
            .date(LocalDate.now())
            .start(LocalTime.of(9, 0))
            .end(LocalTime.of(10, 0))
            .build();

        errors = new SimpleErrors(meeting);
        validator.validate(meeting, errors);

        verifyErrors("title", FIELD_REQUIRED, "Meeting must have a title");
    }

    @Test
    void validate_DateIsEmpty_ShouldRejectDate() {
        Meeting meeting = Meeting.builder()
            .title("Team Meeting")
            .date(null)
            .start(LocalTime.of(9, 0))
            .end(LocalTime.of(10, 0))
            .build();

        errors = new SimpleErrors(meeting);
        validator.validate(meeting, errors);

        verifyErrors("date", FIELD_REQUIRED, "Meeting must have a date");
    }

    @Test
    void validate_StartTimeIsEmpty_ShouldRejectStartTime() {
        Meeting meeting = Meeting.builder()
            .title("Team Meeting")
            .date(LocalDate.now())
            .start(null)
            .end(LocalTime.of(10, 0))
            .build();

        errors = new SimpleErrors(meeting);
        validator.validate(meeting, errors);

        verifyErrors("start", FIELD_REQUIRED, "Meeting must have a start time");
    }

    @Test
    void validate_EndTimeIsEmpty_ShouldRejectEndTime() {
        Meeting meeting = Meeting.builder()
            .title("Team Meeting")
            .date(LocalDate.now())
            .start(LocalTime.of(9, 0))
            .end(null)
            .build();

        errors = new SimpleErrors(meeting);
        validator.validate(meeting, errors);

        verifyErrors("end", FIELD_REQUIRED, "Meeting must have an end time");
    }

    @Test
    void validate_StartAndEndTimeAreEqual_ShouldRejectEndTime() {
        LocalTime time = LocalTime.of(9, 0);
        Meeting meeting = Meeting.builder()
            .title("Team Meeting")
            .date(LocalDate.now())
            .start(time)
            .end(time)
            .build();

        errors = new SimpleErrors(meeting);
        validator.validate(meeting, errors);

        verifyErrors("end", FIELD_INVALID, "Meeting start and end time cannot be equal");
    }

    @Test
    void validate_EndTimeBeforeStartTime_ShouldRejectEndTime() {
        Meeting meeting = Meeting.builder()
            .title("Team Meeting")
            .date(LocalDate.now())
            .start(LocalTime.of(10, 0))
            .end(LocalTime.of(9, 0))
            .build();

        errors = new SimpleErrors(meeting);
        validator.validate(meeting, errors);

        verifyErrors("end", FIELD_INVALID, "Meeting end time must be after start time");
    }

    @Test
    void validate_DurationLessThan15Minutes_ShouldRejectEndTime() {
        Meeting meeting = Meeting.builder()
            .title("Team Meeting")
            .date(LocalDate.now())
            .start(LocalTime.of(9, 0))
            .end(LocalTime.of(9, 10))
            .build();

        errors = new SimpleErrors(meeting);
        validator.validate(meeting, errors);

        verifyErrors("end", FIELD_INVALID, "Meeting time cannot be less than 15 minutes");
    }

    @Test
    void validate_StartTimeBeforeWorkingHours_ShouldRejectStartTime() {
        Meeting meeting = Meeting.builder()
            .title("Team Meeting")
            .date(LocalDate.now())
            .start(LocalTime.of(7, 0))
            .end(LocalTime.of(9, 0))
            .build();

        errors = new SimpleErrors(meeting);
        validator.validate(meeting, errors);

        verifyErrors("start", FIELD_INVALID, "Meeting start time cannot be before " + WORKING_HOURS_START);
    }

    @Test
    void validate_EndTimeAfterWorkingHours_ShouldRejectEndTime() {
        Meeting meeting = Meeting.builder()
            .title("Team Meeting")
            .date(LocalDate.now())
            .start(LocalTime.of(17, 0))
            .end(LocalTime.of(18, 0))
            .build();

        errors = new SimpleErrors(meeting);
        validator.validate(meeting, errors);

        verifyErrors("end", FIELD_INVALID, "Meeting end time cannot be after " + WORKING_HOURS_END);
    }

    private void verifyErrors(String field, SchedulerErrors.ErrorCode errorCode, String message) {
        verifyErrors(field, errorCode.name(), message);
    }

    private void verifyErrors(String field, String errorCode, String message) {
        assertThat(errors).isNotNull();

        List<FieldError> fieldErrors = errors.getFieldErrors(field);
        assertThat(fieldErrors).isNotNull();

        Predicate<String> errorCodePredicate = p -> p.contains(errorCode);
        Predicate<String> messagePredicate = p -> p.contains(message);

        long hits = fieldErrors.stream().flatMap(elem -> Arrays.stream(elem.getCodes()).filter(errorCodePredicate)).count();
        assertThat(hits).isGreaterThanOrEqualTo(1L);

        boolean found = fieldErrors.stream().anyMatch(elem -> messagePredicate.test(elem.getDefaultMessage()));
        assertThat(found).isTrue();
    }
}
