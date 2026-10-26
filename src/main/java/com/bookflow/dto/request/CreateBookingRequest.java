package com.bookflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateBookingRequest(
    @NotNull UUID roomId,
    @NotNull UUID employeeId,
    @NotNull OffsetDateTime start,
    @NotNull OffsetDateTime end,
    @Min(1) int participantCount) {}
