package com.bookflow.service.impl;

import com.bookflow.entity.BookingEntity;
import com.bookflow.entity.BookingStatus;
import com.bookflow.entity.EmployeeEntity;
import com.bookflow.entity.RoomEntity;
import com.bookflow.exception.BookingConflictException;
import com.bookflow.exception.EmployeeNotFoundException;
import com.bookflow.repository.BookingRepository;
import com.bookflow.repository.EmployeeRepository;
import com.bookflow.service.BookingService;
import com.bookflow.service.RoomService;
import com.bookflow.validation.BookingInterval;
import com.bookflow.validation.BookingValidator;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

  private static final String OVERLAP_CONSTRAINT = "booking_no_room_overlap";

  private final BookingRepository bookingRepository;
  private final EmployeeRepository employeeRepository;
  private final RoomService roomService;
  private final Clock clock;
  private final ZoneId officeZone;

  @Transactional(readOnly = true)
  @Override
  public List<RoomEntity> findAvailableRooms(Instant start, Instant end, int participantCount) {
    BookingInterval interval = validate(start, end, participantCount);
    Set<UUID> occupiedRoomIds =
        bookingRepository.findOccupiedRoomIds(
            interval.start(), interval.end(), BookingStatus.CONFIRMED);
    return roomService.findActiveRooms(participantCount).stream()
        .filter(room -> !occupiedRoomIds.contains(room.getId()))
        .toList();
  }

  @Transactional
  @Override
  public BookingEntity createBooking(
      UUID roomId, UUID employeeId, Instant start, Instant end, int participantCount) {
    BookingInterval interval = validate(start, end, participantCount);
    EmployeeEntity employee = requireEmployee(employeeId);
    RoomEntity room = roomService.requireBookableRoom(roomId, participantCount);
    if (bookingRepository.existsByRoom_IdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
        roomId, BookingStatus.CONFIRMED, interval.end(), interval.start())) {
      throw new BookingConflictException();
    }

    BookingEntity booking =
        BookingEntity.confirmed(
            room, employee, interval.start(), interval.end(), participantCount, clock.instant());
    try {
      return bookingRepository.saveAndFlush(booking);
    } catch (DataAccessException exception) {
      if (isOverlapViolation(exception)) {
        throw new BookingConflictException();
      }
      throw exception;
    }
  }

  @Transactional(readOnly = true)
  @Override
  public List<BookingEntity> listUpcomingBookings(UUID employeeId) {
    requireEmployee(employeeId);
    return bookingRepository.findByEmployee_IdAndStatusAndEndAtGreaterThanOrderByStartAtAsc(
        employeeId, BookingStatus.CONFIRMED, clock.instant());
  }

  private BookingInterval validate(Instant start, Instant end, int participantCount) {
    return BookingValidator.validate(start, end, participantCount, clock.instant(), officeZone);
  }

  private EmployeeEntity requireEmployee(UUID employeeId) {
    return employeeRepository
        .findById(employeeId)
        .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
  }

  private static boolean isOverlapViolation(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      if (current instanceof ConstraintViolationException violation
          && OVERLAP_CONSTRAINT.equals(violation.getConstraintName())) {
        return true;
      }
      if (current instanceof SQLException sqlException
          && "23P01".equals(sqlException.getSQLState())
          && sqlException.getMessage() != null
          && sqlException.getMessage().contains(OVERLAP_CONSTRAINT)) {
        return true;
      }
      if (current instanceof SQLException sqlException
          && "40P01".equals(sqlException.getSQLState())) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }
}
