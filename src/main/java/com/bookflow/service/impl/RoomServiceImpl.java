package com.bookflow.service.impl;

import com.bookflow.entity.RoomEntity;
import com.bookflow.entity.RoomStatus;
import com.bookflow.exception.RoomCapacityExceededException;
import com.bookflow.exception.RoomNotBookableException;
import com.bookflow.exception.RoomNotFoundException;
import com.bookflow.repository.RoomRepository;
import com.bookflow.service.RoomService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

  private final RoomRepository roomRepository;

  @Transactional(readOnly = true)
  @Override
  public List<RoomEntity> findActiveRooms(int minimumCapacity) {
    return roomRepository.findByStatusAndCapacityGreaterThanEqualOrderByCapacityAscNameAsc(
        RoomStatus.ACTIVE, minimumCapacity);
  }

  @Transactional(readOnly = true)
  @Override
  public RoomEntity requireBookableRoom(UUID roomId, int participantCount) {
    RoomEntity room =
        roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
    if (!room.isActive()) {
      throw new RoomNotBookableException(roomId);
    }
    if (room.getCapacity() < participantCount) {
      throw new RoomCapacityExceededException(roomId, participantCount, room.getCapacity());
    }
    return room;
  }
}
