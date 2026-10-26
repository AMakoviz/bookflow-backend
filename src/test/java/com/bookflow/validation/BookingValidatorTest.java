package com.bookflow.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookflow.exception.BookingRuleViolationException;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class BookingValidatorTest {

  private static final ZoneId OFFICE_ZONE = ZoneId.of("Europe/Moscow");
  private static final Instant NOW = Instant.parse("2026-10-25T07:00:00Z");

  @Test
  void acceptsValidInterval() {
    assertDoesNotThrow(
        () ->
            BookingValidator.validate(
                Instant.parse("2026-10-26T07:00:00Z"),
                Instant.parse("2026-10-26T08:00:00Z"),
                4,
                NOW,
                OFFICE_ZONE));
  }

  @Test
  void rejectsPastStart() {
    BookingRuleViolationException exception =
        assertThrows(
            BookingRuleViolationException.class,
            () ->
                BookingValidator.validate(
                    Instant.parse("2026-10-25T06:30:00Z"),
                    Instant.parse("2026-10-25T07:30:00Z"),
                    4,
                    NOW,
                    OFFICE_ZONE));

    assertEquals("BOOKING_IN_PAST", exception.code());
  }

  @Test
  void rejectsInvalidIntervalOrder() {
    BookingRuleViolationException exception =
        assertThrows(
            BookingRuleViolationException.class,
            () ->
                BookingValidator.validate(
                    Instant.parse("2026-10-26T08:00:00Z"),
                    Instant.parse("2026-10-26T07:00:00Z"),
                    2,
                    NOW,
                    OFFICE_ZONE));

    assertEquals("INVALID_INTERVAL", exception.code());
  }

  @Test
  void rejectsInvalidParticipantCount() {
    BookingRuleViolationException exception =
        assertThrows(
            BookingRuleViolationException.class,
            () ->
                BookingValidator.validate(
                    Instant.parse("2026-10-26T07:00:00Z"),
                    Instant.parse("2026-10-26T08:00:00Z"),
                    0,
                    NOW,
                    OFFICE_ZONE));

    assertEquals("INVALID_PARTICIPANT_COUNT", exception.code());
  }

  @Test
  void rejectsIntervalsOutsideDurationRange() {
    BookingRuleViolationException shortBooking =
        assertThrows(
            BookingRuleViolationException.class,
            () ->
                BookingValidator.validate(
                    Instant.parse("2026-10-26T07:00:00Z"),
                    Instant.parse("2026-10-26T07:00:00Z").plusSeconds(15 * 60),
                    2,
                    NOW,
                    OFFICE_ZONE));
    BookingRuleViolationException longBooking =
        assertThrows(
            BookingRuleViolationException.class,
            () ->
                BookingValidator.validate(
                    Instant.parse("2026-10-26T07:00:00Z"),
                    Instant.parse("2026-10-26T11:30:00Z"),
                    2,
                    NOW,
                    OFFICE_ZONE));

    assertEquals("INVALID_TIME_INCREMENT", shortBooking.code());
    assertEquals("INVALID_DURATION", longBooking.code());
  }

  @Test
  void rejectsInvalidIncrementCrossDayWorkingHoursAndHorizon() {
    assertEquals(
        "INVALID_TIME_INCREMENT", violation("2026-10-26T07:10:00Z", "2026-10-26T08:10:00Z").code());
    assertEquals(
        "CROSS_DAY_BOOKING", violation("2026-10-26T20:30:00Z", "2026-10-26T21:30:00Z").code());
    assertEquals(
        "OUTSIDE_WORKING_HOURS", violation("2026-10-26T04:30:00Z", "2026-10-26T05:30:00Z").code());
    assertEquals(
        "OUTSIDE_BOOKING_HORIZON",
        violation("2026-11-25T07:00:00Z", "2026-11-25T08:00:00Z").code());
  }

  @Test
  void usesHalfOpenOverlapSemantics() {
    BookingInterval existing =
        new BookingInterval(
            Instant.parse("2026-10-26T07:00:00Z"), Instant.parse("2026-10-26T08:00:00Z"));
    BookingInterval overlapping =
        new BookingInterval(
            Instant.parse("2026-10-26T07:30:00Z"), Instant.parse("2026-10-26T08:30:00Z"));
    BookingInterval adjacent =
        new BookingInterval(
            Instant.parse("2026-10-26T08:00:00Z"), Instant.parse("2026-10-26T08:30:00Z"));

    assertTrue(existing.overlaps(overlapping));
    assertFalse(existing.overlaps(adjacent));
  }

  private static BookingRuleViolationException violation(String start, String end) {
    return assertThrows(
        BookingRuleViolationException.class,
        () ->
            BookingValidator.validate(
                Instant.parse(start), Instant.parse(end), 2, NOW, OFFICE_ZONE));
  }
}
