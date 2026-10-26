package com.bookflow.dto.response;

import java.util.UUID;

public record RoomResponse(UUID id, String name, String location, int capacity) {}
