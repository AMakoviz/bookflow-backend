package com.bookflow.exception;

import java.util.UUID;

public final class RoomCapacityExceededException extends RuntimeException {

  public RoomCapacityExceededException(UUID roomId, int requested, int capacity) {
    super(
        "Room %s has capacity %d, but %d participants were requested"
            .formatted(roomId, capacity, requested));
  }
}
