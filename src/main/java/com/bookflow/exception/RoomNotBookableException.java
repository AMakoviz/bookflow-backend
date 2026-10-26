package com.bookflow.exception;

import java.util.UUID;

public final class RoomNotBookableException extends RuntimeException {

  public RoomNotBookableException(UUID roomId) {
    super("Room %s is not active".formatted(roomId));
  }
}
