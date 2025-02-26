package se.callistaenterprise.scheduler.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import se.callistaenterprise.scheduler.exception.BadRequestException;
import se.callistaenterprise.scheduler.exception.NotFoundException;
import se.callistaenterprise.scheduler.model.ErrorResponse;
import se.callistaenterprise.scheduler.validation.ValidationException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final String NOT_FOUND_MESSAGE = "The resource you looked for couldn't be found!";
  private static final String GENERIC_SERVER_ERROR_MESSAGE =
      "Something went wrong and we are trying to fix it!";

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public @ResponseBody ErrorResponse handleNotFoundException(NotFoundException ex) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), NOT_FOUND_MESSAGE);
  }

  @ExceptionHandler({
    BadRequestException.class,
    ValidationException.class,
    MissingServletRequestParameterException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public @ResponseBody ErrorResponse handleBadRequestExceptions(RuntimeException ex) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public @ResponseBody ErrorResponse handleGenericException(Exception ex) {
    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), GENERIC_SERVER_ERROR_MESSAGE);
  }

  private ErrorResponse buildErrorResponse(HttpStatus status, String errorMessage) {
    return buildErrorResponse(status, errorMessage, null);
  }

  private ErrorResponse buildErrorResponse(
      HttpStatus status, String errorMessage, String friendlyMessage) {
    log.error(errorMessage);
    return StringUtils.hasText(friendlyMessage)
        ? new ErrorResponse(status.value(), friendlyMessage)
        : new ErrorResponse(status.value(), errorMessage);
  }
}
