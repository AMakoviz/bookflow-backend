# API Conventions

| Field | Value |
|---|---|
| Status | Accepted |

## 1. General

- Use JSON over HTTP for the browser-facing API with `application/json` request and response content types.
- Spring MVC and Jackson deserialize request DTOs and serialize response DTOs. JPA entities are never JSON payloads.
- Controllers return DTOs directly. Use `@ResponseStatus` for fixed statuses and explicit response headers when required; ordinary DTO responses are not wrapped in `ResponseEntity`.
- Describe contracts with OpenAPI.
- Prefix HTTP paths with `/api/v1`.
- Use nouns for resources and actions only when they do not map cleanly to resource state.
- Use UUIDs as identifiers.
- Use RFC 3339 timestamps with explicit offsets.
- Treat booking intervals as half-open `[start, end)`.
- Internal backend collaboration uses direct service calls rather than internal HTTP.

## 2. Status codes

| Situation | Status |
|---|---:|
| Successful query | 200 |
| Resource created | 201 |
| Invalid request shape or business input | 400 |
| Resource not found | 404 |
| Booking interval conflict | 409 |
| Dependency temporarily unavailable | 503 |

## 3. Error representation

Errors use a stable problem payload containing:

- `type`
- `title`
- `status`
- `detail`
- `instance`
- `code`
- `correlationId`
- field violations when applicable

Stack traces and internal exception messages must not be returned to clients.

## 4. Idempotency and concurrency

- Booking creation will support an idempotency key before external retries or mobile clients are introduced.
- Database constraints remain authoritative for concurrent booking conflicts.
- Retrying clients must not convert a conflict into a duplicate booking.

## 5. Compatibility

- Additive response fields are allowed within a major API version.
- Removing or renaming fields requires a new version or an explicit migration plan.
- Service interfaces and DTO contracts change through deliberate refactoring with architecture tests kept green.

