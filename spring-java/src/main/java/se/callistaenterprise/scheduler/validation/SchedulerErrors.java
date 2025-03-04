package se.callistaenterprise.scheduler.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;

public class SchedulerErrors extends SimpleErrors {

  public SchedulerErrors(Object target) {
    super(target);
  }

  public SchedulerErrors(Object target, String objectName) {
    super(target, objectName);
  }

  public static <V> Errors createErrors(
      V target, String field, ErrorCode errorCode, String message) {
    Errors errors = new SchedulerErrors(target);
    errors.rejectValue(field, errorCode.name(), message);
    return errors;
  }

  public static <V> Errors createErrors(V target, String field, String errorCode, String message) {
    Errors errors = new SchedulerErrors(target);
    errors.rejectValue(field, errorCode, message);
    return errors;
  }

  public enum ErrorCode {
    FIELD_REQUIRED("field.required"),
    FIELD_INVALID("field.invalid"),
    TIME_NOT_AVAILABLE("time.not_available"),
    RESOURCE_NOT_FOUND("resource.not_found"),
    INSERT_FAILED("resource.insert_failed"),
    ;

    private final String code;

    ErrorCode(String code) {
      this.code = code;
    }

    public String code() {
      return this.code;
    }
  }
}
