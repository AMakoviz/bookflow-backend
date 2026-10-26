package com.bookflow.exception;

public final class BookingConflictException extends RuntimeException {

  public BookingConflictException() {
    super("The room has already been booked for part of the requested interval");
  }
}
