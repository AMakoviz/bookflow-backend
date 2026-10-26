package com.bookflow.service;

import com.bookflow.entity.RoomEntity;
import java.util.List;
import java.util.UUID;

public interface RoomService {

  List<RoomEntity> findActiveRooms(int minimumCapacity);

  RoomEntity requireBookableRoom(UUID roomId, int participantCount);
}
