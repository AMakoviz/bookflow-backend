package com.bookflow.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    RoomResponse room,
    UUID employeeId,
    OffsetDateTime start,
    OffsetDateTime end,
    int participantCount,
    BookingStatusResponse status,
    OffsetDateTime createdAt) {}
