package com.bookflow.controller;

import com.bookflow.dto.request.CreateBookingRequest;
import com.bookflow.dto.response.BookingListResponse;
import com.bookflow.dto.response.BookingResponse;
import com.bookflow.mapper.BookingMapper;
import com.bookflow.service.BookingService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {

  private final BookingService bookingService;
  private final BookingMapper bookingMapper;
  private final ZoneId officeZone;

  @PostMapping("/bookings")
  @ResponseStatus(HttpStatus.CREATED)
  BookingResponse createBooking(
      @Valid @RequestBody CreateBookingRequest request, HttpServletResponse httpResponse) {
    BookingResponse response =
        bookingMapper.toResponse(
            bookingService.createBooking(
                request.roomId(),
                request.employeeId(),
                request.start().toInstant(),
                request.end().toInstant(),
                request.participantCount()),
            officeZone);
    httpResponse.setHeader(HttpHeaders.LOCATION, "/api/v1/bookings/" + response.id());
    return response;
  }

  @GetMapping("/employees/{employeeId}/bookings")
  BookingListResponse listBookings(@PathVariable UUID employeeId) {
    return new BookingListResponse(
        bookingService.listUpcomingBookings(employeeId).stream()
            .map(booking -> bookingMapper.toResponse(booking, officeZone))
            .toList());
  }
}
