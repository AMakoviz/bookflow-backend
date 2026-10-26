package com.bookflow.validation;

import com.bookflow.exception.BookingRuleViolationException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class BookingValidator {

  private static final Duration MINIMUM_DURATION = Duration.ofMinutes(30);
  private static final Duration MAXIMUM_DURATION = Duration.ofHours(4);
  private static final LocalTime WORKING_DAY_START = LocalTime.of(8, 0);
  private static final LocalTime WORKING_DAY_END = LocalTime.of(22, 0);
  private static final int BOOKING_HORIZON_DAYS = 30;

  private BookingValidator() {}

  public static BookingInterval validate(
      Instant start, Instant end, int participantCount, Instant now, ZoneId officeZone) {
    if (start == null) {
      throw violation("START_REQUIRED", "start", "Start time is required");
    }
    if (end == null) {
      throw violation("END_REQUIRED", "end", "End time is required");
    }
    if (!start.isBefore(end)) {
      throw violation("INVALID_INTERVAL", "end", "End time must be after start time");
    }
    if (!start.isAfter(now)) {
      throw violation("BOOKING_IN_PAST", "start", "Booking start must be in the future");
    }
    if (participantCount < 1) {
      throw violation(
          "INVALID_PARTICIPANT_COUNT",
          "participantCount",
          "Participant count must be at least one");
    }

    ZonedDateTime localStart = start.atZone(officeZone);
    ZonedDateTime localEnd = end.atZone(officeZone);
    if (!localStart.toLocalDate().equals(localEnd.toLocalDate())) {
      throw violation(
          "CROSS_DAY_BOOKING", "end", "Booking must start and end on the same office day");
    }
    if (!isOnHalfHour(localStart) || !isOnHalfHour(localEnd)) {
      throw violation(
          "INVALID_TIME_INCREMENT", "start", "Start and end must align to 30-minute boundaries");
    }

    Duration duration = Duration.between(start, end);
    if (duration.compareTo(MINIMUM_DURATION) < 0 || duration.compareTo(MAXIMUM_DURATION) > 0) {
      throw violation(
          "INVALID_DURATION", "end", "Booking duration must be between 30 minutes and 4 hours");
    }
    if (localStart.toLocalTime().isBefore(WORKING_DAY_START)
        || localEnd.toLocalTime().isAfter(WORKING_DAY_END)) {
      throw violation(
          "OUTSIDE_WORKING_HOURS", "start", "Booking must be within office hours 08:00-22:00");
    }

    LocalDate latestDate = now.atZone(officeZone).toLocalDate().plusDays(BOOKING_HORIZON_DAYS);
    if (localStart.toLocalDate().isAfter(latestDate)) {
      throw violation(
          "OUTSIDE_BOOKING_HORIZON", "start", "Booking cannot start more than 30 days in advance");
    }

    return new BookingInterval(start, end);
  }

  private static boolean isOnHalfHour(ZonedDateTime value) {
    return value.getMinute() % 30 == 0 && value.getSecond() == 0 && value.getNano() == 0;
  }

  private static BookingRuleViolationException violation(
      String code, String field, String message) {
    return new BookingRuleViolationException(code, field, message);
  }
}
