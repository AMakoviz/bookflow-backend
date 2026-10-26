package com.bookflow.repository;

import com.bookflow.entity.BookingEntity;
import com.bookflow.entity.BookingStatus;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

  boolean existsByRoom_IdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
      UUID roomId, BookingStatus status, Instant requestedEnd, Instant requestedStart);

  @EntityGraph(attributePaths = {"room", "employee"})
  List<BookingEntity> findByEmployee_IdAndStatusAndEndAtGreaterThanOrderByStartAtAsc(
      UUID employeeId, BookingStatus status, Instant now);

  @Query(
      """
      select distinct booking.room.id
      from BookingEntity booking
      where booking.status = :status
        and booking.startAt < :requestedEnd
        and booking.endAt > :requestedStart
      """)
  Set<UUID> findOccupiedRoomIds(
      @Param("requestedStart") Instant requestedStart,
      @Param("requestedEnd") Instant requestedEnd,
      @Param("status") BookingStatus status);
}
