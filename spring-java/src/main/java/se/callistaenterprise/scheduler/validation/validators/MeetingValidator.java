package se.callistaenterprise.scheduler.validation.validators;

import static se.callistaenterprise.scheduler.validation.SchedulerErrors.ErrorCode.FIELD_INVALID;
import static se.callistaenterprise.scheduler.validation.SchedulerErrors.ErrorCode.FIELD_REQUIRED;

import java.time.Duration;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import se.callistaenterprise.scheduler.config.SchedulerProperties;
import se.callistaenterprise.scheduler.entity.Meeting;
import se.callistaenterprise.scheduler.validation.SchedulerErrors;

public class MeetingValidator implements Validator {

  private final SchedulerProperties schedulerProperties;

  public MeetingValidator(SchedulerProperties schedulerProperties) {
    this.schedulerProperties = schedulerProperties;
  }

  @Override
  public boolean supports(Class<?> cls) {
    return Meeting.class.isAssignableFrom(cls);
  }

  @Override
  public void validate(Object target, Errors errors) {
    if (!supports(target.getClass())) {
      throw new IllegalArgumentException("target cannot be assigned to Meeting class");
    }

    // Required fields
    rejectIfEmptyOrWhitespace(errors, "title", FIELD_REQUIRED, "Meeting must have a title");
    rejectIfEmptyOrWhitespace(errors, "date", FIELD_REQUIRED, "Meeting must have a date");
    rejectIfEmptyOrWhitespace(errors, "start", FIELD_REQUIRED, "Meeting must have a start time");
    rejectIfEmptyOrWhitespace(errors, "end", FIELD_REQUIRED, "Meeting must have an end time");

    if (errors.hasErrors()) {
      return;
    }

    Meeting meeting = (Meeting) target;

    // Check valid start and end times
    long differenceInMinutes = Duration.between(meeting.getStart(), meeting.getEnd()).toMinutes();
    if (differenceInMinutes == 0) {
      errors.rejectValue("end", FIELD_INVALID.name(), "Meeting start and end time cannot be equal");
    }
    if (differenceInMinutes < 0) {
      errors.rejectValue("end", FIELD_INVALID.name(), "Meeting end time must be after start time");
    }
    if (differenceInMinutes < 15) {
      errors.rejectValue(
          "end", FIELD_INVALID.name(), "Meeting time cannot be less than 15 minutes");
    }

    // Check meeting is within working hours
    if (meeting.getStart().isBefore(schedulerProperties.getWorkingHours().getStart())) {
      errors.rejectValue(
          "start",
          FIELD_INVALID.name(),
          "Meeting start time cannot be before "
              + schedulerProperties.getWorkingHours().getStart());
    }
    if (meeting.getEnd().isAfter(schedulerProperties.getWorkingHours().getEnd())) {
      errors.rejectValue(
          "end",
          FIELD_INVALID.name(),
          "Meeting end time cannot be after " + schedulerProperties.getWorkingHours().getEnd());
    }
  }

  private static void rejectIfEmptyOrWhitespace(
      Errors errors, String field, SchedulerErrors.ErrorCode errorCode, String defaultMessage) {
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, field, errorCode.name(), defaultMessage);
  }
}
