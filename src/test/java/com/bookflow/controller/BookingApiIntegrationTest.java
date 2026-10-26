package com.bookflow.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookflow.TestcontainersConfiguration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
class BookingApiIntegrationTest {

  private static final String DEMO_EMPLOYEE_ID = "3ceebbd9-84e9-40df-8123-b93128f79ac5";
  private static final String FOCUS_ROOM_ID = "8a8d73f1-1ef0-4e49-aef3-d84ee901a5b5";
  private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000001";
  private static final ZoneId OFFICE_ZONE = ZoneId.of("Europe/Moscow");

  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void clearBookings() {
    jdbcTemplate.update("DELETE FROM booking.bookings");
    jdbcTemplate.update("UPDATE resource.rooms SET status = 'ACTIVE'");
  }

  @Test
  void exposesCapacityMatchedAvailability() throws Exception {
    ZonedDateTime start = futureAt(10, 0);

    mockMvc
        .perform(
            get("/api/v1/rooms/availability")
                .param("start", start.toOffsetDateTime().toString())
                .param("end", start.plusHours(1).toOffsetDateTime().toString())
                .param("participants", "6"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Correlation-ID"))
        .andExpect(jsonPath("$.rooms.length()").value(2))
        .andExpect(jsonPath("$.rooms[0].capacity").value(8))
        .andExpect(jsonPath("$.rooms[1].capacity").value(12));
  }

  @Test
  void createsListsAndRejectsAnOverlappingBooking() throws Exception {
    ZonedDateTime start = futureAt(12, 0);
    String request = requestBody(start, start.plusHours(1));

    mockMvc
        .perform(post("/api/v1/bookings").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(
            header().string("Location", org.hamcrest.Matchers.startsWith("/api/v1/bookings/")))
        .andExpect(jsonPath("$.room.name").value("Focus"))
        .andExpect(jsonPath("$.status").value("CONFIRMED"));

    mockMvc
        .perform(get("/api/v1/employees/{employeeId}/bookings", DEMO_EMPLOYEE_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookings.length()").value(1))
        .andExpect(jsonPath("$.bookings[0].room.name").value("Focus"));

    mockMvc
        .perform(post("/api/v1/bookings").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("BOOKING_CONFLICT"))
        .andExpect(jsonPath("$.correlationId").isNotEmpty());
  }

  @Test
  void returnsStableProblemsForInvalidInput() throws Exception {
    ZonedDateTime start = futureAt(14, 0);

    mockMvc
        .perform(
            get("/api/v1/rooms/availability")
                .param("start", start.toOffsetDateTime().toString())
                .param("end", start.plusHours(1).toOffsetDateTime().toString())
                .param("participants", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.correlationId").isNotEmpty());
  }

  @Test
  void rejectsUnknownJsonFields() throws Exception {
    ZonedDateTime start = futureAt(14, 30);
    String request =
        requestBody(start, start.plusHours(1))
            .replace("\"participantCount\": 4", "\"participantCount\": 4, \"unknown\": true");

    mockMvc
        .perform(post("/api/v1/bookings").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

    assertEquals(0, bookingCount());
  }

  @Test
  void rejectsInvalidIntervalOrderWithoutWritingABooking() throws Exception {
    ZonedDateTime start = futureAt(15, 0);

    mockMvc
        .perform(
            post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody(start, start.minusMinutes(30))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INTERVAL"))
        .andExpect(jsonPath("$.violations[0].field").value("end"));

    assertEquals(0, bookingCount());
  }

  @Test
  void rejectsUnknownInactiveAndUndersizedRooms() throws Exception {
    ZonedDateTime start = futureAt(16, 0);

    mockMvc
        .perform(
            post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody(UNKNOWN_ID, DEMO_EMPLOYEE_ID, start, start.plusHours(1), 2)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

    jdbcTemplate.update(
        "UPDATE resource.rooms SET status = 'INACTIVE' WHERE id = ?::uuid", FOCUS_ROOM_ID);
    mockMvc
        .perform(
            post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody(start, start.plusHours(1))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ROOM_NOT_BOOKABLE"));

    jdbcTemplate.update(
        "UPDATE resource.rooms SET status = 'ACTIVE' WHERE id = ?::uuid", FOCUS_ROOM_ID);
    mockMvc
        .perform(
            post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    requestBody(FOCUS_ROOM_ID, DEMO_EMPLOYEE_ID, start, start.plusHours(1), 5)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ROOM_CAPACITY_EXCEEDED"));

    assertEquals(0, bookingCount());
  }

  @Test
  void rejectsUnknownEmployeeForCreationAndList() throws Exception {
    ZonedDateTime start = futureAt(17, 0);

    mockMvc
        .perform(
            post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody(FOCUS_ROOM_ID, UNKNOWN_ID, start, start.plusHours(1), 2)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

    mockMvc
        .perform(get("/api/v1/employees/{employeeId}/bookings", UNKNOWN_ID))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

    assertEquals(0, bookingCount());
  }

  @Test
  void publishesTheOpenApiDocument() throws Exception {
    mockMvc
        .perform(get("/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("BookFlow API"))
        .andExpect(jsonPath("$.paths['/api/v1/bookings']").exists())
        .andExpect(jsonPath("$.paths['/api/v1/rooms/availability']").exists());
  }

  private static String requestBody(ZonedDateTime start, ZonedDateTime end) {
    return requestBody(FOCUS_ROOM_ID, DEMO_EMPLOYEE_ID, start, end, 4);
  }

  private static String requestBody(
      String roomId,
      String employeeId,
      ZonedDateTime start,
      ZonedDateTime end,
      int participantCount) {
    return """
				{
				  "roomId": "%s",
				  "employeeId": "%s",
				  "start": "%s",
				  "end": "%s",
				  "participantCount": %d
				}
				"""
        .formatted(
            roomId, employeeId, start.toOffsetDateTime(), end.toOffsetDateTime(), participantCount);
  }

  private int bookingCount() {
    return jdbcTemplate.queryForObject("SELECT count(*) FROM booking.bookings", Integer.class);
  }

  private static ZonedDateTime futureAt(int hour, int minute) {
    return ZonedDateTime.of(
        LocalDate.now(OFFICE_ZONE).plusDays(2), LocalTime.of(hour, minute), OFFICE_ZONE);
  }
}
