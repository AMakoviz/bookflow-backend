package com.bookflow.controller;

import com.bookflow.dto.response.AvailabilityResponse;
import com.bookflow.mapper.RoomMapper;
import com.bookflow.service.BookingService;
import jakarta.validation.constraints.Min;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class AvailabilityController {

  private final BookingService bookingService;
  private final RoomMapper roomMapper;

  @GetMapping("/availability")
  AvailabilityResponse findAvailableRooms(
      @RequestParam OffsetDateTime start,
      @RequestParam OffsetDateTime end,
      @RequestParam @Min(1) int participants) {
    return new AvailabilityResponse(
        bookingService.findAvailableRooms(start.toInstant(), end.toInstant(), participants).stream()
            .map(roomMapper::toResponse)
            .toList());
  }
}
