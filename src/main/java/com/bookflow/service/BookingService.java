package com.bookflow.service;

import com.bookflow.entity.BookingEntity;
import com.bookflow.entity.RoomEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingService {

  List<RoomEntity> findAvailableRooms(Instant start, Instant end, int participantCount);

  BookingEntity createBooking(
      UUID roomId, UUID employeeId, Instant start, Instant end, int participantCount);

  List<BookingEntity> listUpcomingBookings(UUID employeeId);
}
