package com.bookflow.dto.response;

import java.net.URI;
import java.util.List;

public record ProblemResponse(
    URI type,
    String title,
    int status,
    String detail,
    String instance,
    String code,
    String correlationId,
    List<ViolationResponse> violations) {}
