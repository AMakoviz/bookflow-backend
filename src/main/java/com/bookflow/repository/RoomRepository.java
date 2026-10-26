package com.bookflow.repository;

import com.bookflow.entity.RoomEntity;
import com.bookflow.entity.RoomStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {

  List<RoomEntity> findByStatusAndCapacityGreaterThanEqualOrderByCapacityAscNameAsc(
      RoomStatus status, int minimumCapacity);
}
