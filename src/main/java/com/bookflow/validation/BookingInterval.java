package com.bookflow.validation;

import java.time.Instant;
import java.util.Objects;

public record BookingInterval(Instant start, Instant end) {

  public BookingInterval {
    Objects.requireNonNull(start, "start must not be null");
    Objects.requireNonNull(end, "end must not be null");
  }

  public boolean overlaps(BookingInterval other) {
    return start.isBefore(other.end) && end.isAfter(other.start);
  }
}
