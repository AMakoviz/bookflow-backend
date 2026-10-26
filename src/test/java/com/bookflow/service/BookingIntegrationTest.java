package com.bookflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookflow.TestcontainersConfiguration;
import com.bookflow.entity.BookingEntity;
import com.bookflow.entity.RoomEntity;
import com.bookflow.exception.BookingConflictException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BookingIntegrationTest {

  private static final UUID DEMO_EMPLOYEE_ID =
      UUID.fromString("3ceebbd9-84e9-40df-8123-b93128f79ac5");
  private static final UUID FOCUS_ROOM_ID = UUID.fromString("8a8d73f1-1ef0-4e49-aef3-d84ee901a5b5");
  private static final ZoneId OFFICE_ZONE = ZoneId.of("Europe/Moscow");

  @Autowired private BookingService bookingService;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void clearBookings() {
    jdbcTemplate.update("DELETE FROM booking.bookings");
  }

  @Test
  void filtersByCapacityAndOverlapAndAllowsAdjacentBooking() {
    ZonedDateTime start = futureAt(10, 0);
    ZonedDateTime end = start.plusHours(1);
    createBooking(start, end, 4);

    List<RoomEntity> overlapping =
        bookingService.findAvailableRooms(
            start.plusMinutes(30).toInstant(), end.plusMinutes(30).toInstant(), 4);
    List<RoomEntity> adjacent =
        bookingService.findAvailableRooms(end.toInstant(), end.plusMinutes(30).toInstant(), 4);
    List<RoomEntity> capacityMatched =
        bookingService.findAvailableRooms(end.toInstant(), end.plusMinutes(30).toInstant(), 6);

    assertTrue(overlapping.stream().noneMatch(room -> room.getId().equals(FOCUS_ROOM_ID)));
    assertTrue(adjacent.stream().anyMatch(room -> room.getId().equals(FOCUS_ROOM_ID)));
    assertEquals(List.of(8, 12), capacityMatched.stream().map(RoomEntity::getCapacity).toList());
  }

  @Test
  void persistsBookingAndListsItImmediately() {
    ZonedDateTime start = futureAt(13, 0);
    BookingEntity created = createBooking(start, start.plusHours(1), 3);

    List<BookingEntity> bookings = bookingService.listUpcomingBookings(DEMO_EMPLOYEE_ID);

    assertEquals(1, bookings.size());
    assertEquals(created.getId(), bookings.getFirst().getId());
    assertEquals("Focus", bookings.getFirst().getRoom().getName());
  }

  @Test
  void concurrentOverlappingRequestsProduceOneSuccessAndOneConflict() throws Exception {
    ZonedDateTime start = futureAt(15, 0);
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch fire = new CountDownLatch(1);

    try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
      List<Future<String>> results = new ArrayList<>();
      for (int index = 0; index < 2; index++) {
        results.add(
            executor.submit(
                () -> {
                  ready.countDown();
                  fire.await();
                  try {
                    createBooking(start, start.plusHours(1), 2);
                    return "created";
                  } catch (BookingConflictException exception) {
                    return "conflict";
                  }
                }));
      }
      ready.await();
      fire.countDown();

      List<String> outcomes = results.stream().map(BookingIntegrationTest::get).toList();
      assertEquals(1, outcomes.stream().filter("created"::equals).count());
      assertEquals(1, outcomes.stream().filter("conflict"::equals).count());
    }

    Integer persisted =
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM booking.bookings WHERE room_id = ?",
            Integer.class,
            FOCUS_ROOM_ID);
    assertEquals(1, persisted);
  }

  private BookingEntity createBooking(
      ZonedDateTime start, ZonedDateTime end, int participantCount) {
    return bookingService.createBooking(
        FOCUS_ROOM_ID, DEMO_EMPLOYEE_ID, start.toInstant(), end.toInstant(), participantCount);
  }

  private static ZonedDateTime futureAt(int hour, int minute) {
    return ZonedDateTime.of(
        LocalDate.now(OFFICE_ZONE).plusDays(1), LocalTime.of(hour, minute), OFFICE_ZONE);
  }

  private static String get(Future<String> future) {
    try {
      return future.get();
    } catch (Exception exception) {
      throw new AssertionError(exception);
    }
  }
}
