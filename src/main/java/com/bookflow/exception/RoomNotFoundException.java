package com.bookflow.exception;

import java.util.UUID;

public final class RoomNotFoundException extends RuntimeException {

  public RoomNotFoundException(UUID roomId) {
    super("Room %s was not found".formatted(roomId));
  }
}
