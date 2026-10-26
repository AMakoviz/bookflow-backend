package com.bookflow.handler;

import com.bookflow.dto.response.ProblemResponse;
import com.bookflow.dto.response.ViolationResponse;
import com.bookflow.exception.BookingConflictException;
import com.bookflow.exception.BookingRuleViolationException;
import com.bookflow.exception.EmployeeNotFoundException;
import com.bookflow.exception.RoomCapacityExceededException;
import com.bookflow.exception.RoomNotBookableException;
import com.bookflow.exception.RoomNotFoundException;
import com.bookflow.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public final class ApiExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(BookingRuleViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ProblemResponse handleRuleViolation(
      BookingRuleViolationException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.BAD_REQUEST,
        exception.code(),
        "Booking request is invalid",
        exception.getMessage(),
        List.of(new ViolationResponse(exception.field(), exception.getMessage())),
        request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ProblemResponse handleBodyValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    List<ViolationResponse> violations =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> new ViolationResponse(error.getField(), error.getDefaultMessage()))
            .toList();
    return problem(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Request validation failed",
        "One or more request fields are invalid.",
        violations,
        request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ProblemResponse handleParameterValidation(
      ConstraintViolationException exception, HttpServletRequest request) {
    List<ViolationResponse> violations =
        exception.getConstraintViolations().stream()
            .map(
                violation ->
                    new ViolationResponse(
                        violation.getPropertyPath().toString(), violation.getMessage()))
            .toList();
    return problem(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Request validation failed",
        "One or more request parameters are invalid.",
        violations,
        request);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ProblemResponse handleMalformedRequest(Exception exception, HttpServletRequest request) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "MALFORMED_REQUEST",
        "Request could not be read",
        "Check required fields, identifiers, and RFC 3339 timestamps.",
        List.of(),
        request);
  }

  @ExceptionHandler({RoomNotFoundException.class, EmployeeNotFoundException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ProblemResponse handleNotFound(RuntimeException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.NOT_FOUND,
        "RESOURCE_NOT_FOUND",
        "Resource was not found",
        exception.getMessage(),
        List.of(),
        request);
  }

  @ExceptionHandler(RoomNotBookableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ProblemResponse handleRoomNotBookable(
      RoomNotBookableException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "ROOM_NOT_BOOKABLE",
        "Room cannot be booked",
        exception.getMessage(),
        List.of(),
        request);
  }

  @ExceptionHandler(RoomCapacityExceededException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ProblemResponse handleCapacityExceeded(
      RoomCapacityExceededException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "ROOM_CAPACITY_EXCEEDED",
        "Room capacity is insufficient",
        exception.getMessage(),
        List.of(),
        request);
  }

  @ExceptionHandler(BookingConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  ProblemResponse handleConflict(BookingConflictException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.CONFLICT,
        "BOOKING_CONFLICT",
        "Room is no longer available",
        exception.getMessage(),
        List.of(),
        request);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  ProblemResponse handleUnexpected(Exception exception, HttpServletRequest request) {
    LOGGER.error("Unhandled API failure", exception);
    return problem(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "Unexpected server error",
        "The request could not be completed.",
        List.of(),
        request);
  }

  private static ProblemResponse problem(
      HttpStatus status,
      String code,
      String title,
      String detail,
      List<ViolationResponse> violations,
      HttpServletRequest request) {
    String correlationId =
        String.valueOf(request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE));
    return new ProblemResponse(
        URI.create("https://bookflow.local/problems/" + code.toLowerCase().replace('_', '-')),
        title,
        status.value(),
        detail,
        request.getRequestURI(),
        code,
        correlationId,
        violations);
  }
}
