package se.callistaenterprise.scheduler.validation;

import org.springframework.validation.Errors;

public class Validator {

  public static <V> Errors validate(V target, org.springframework.validation.Validator validator) {
    if (validator == null) {
      throw new IllegalArgumentException(
          "The supplied [Validator] is " + "required and must not be null.");
    }
    if (!validator.supports(target.getClass())) {
      throw new IllegalArgumentException(
          "The supplied [Validator] must "
              + "support the validation of "
              + target.getClass().getSimpleName()
              + "instances.");
    }

    Errors errors = new SchedulerErrors(target);
    validator.validate(target, errors);

    return errors;
  }
}
