# Testing Strategy

| Field | Value |
|---|---|
| Status | Accepted |

## 1. Test layers

### Domain unit tests

Fast tests for interval rules, booking horizon, capacity requirements, and value objects. They must not start Spring or access a database.

### Application tests

Tests for service orchestration: room lookup, booking persistence, validation, and error translation.

### Persistence integration tests

Testcontainers-backed PostgreSQL tests for Flyway migrations, repository mappings, queries, and the exclusion constraint. An in-memory database is not acceptable for these tests.

### HTTP integration tests

Tests for request validation, stable status codes, problem payloads, and OpenAPI compatibility.

### Module architecture tests

ArchUnit verifies dependency direction between controllers, service contracts, service implementations, repositories, entities, DTOs, MapStruct mappers, and validation. It also prevents controllers from importing `service.impl` classes.

### End-to-end test

One browser-level scenario proves the first vertical slice through the web application, layered BookFlow backend, and PostgreSQL.

## 2. Mandatory first-feature scenarios

- Capacity-matching available rooms are returned.
- Occupied rooms are excluded for overlapping intervals.
- Adjacent bookings are permitted.
- Past intervals are rejected.
- Invalid intervals are rejected.
- Unknown and inactive rooms cannot be booked.
- A confirmed booking appears in the user's booking list.
- Two concurrent overlapping booking requests result in exactly one success and one conflict.
- Module architecture rules pass.

## 3. Evidence

The active Evidence Bundle records:

- Commands executed.
- Test totals and outcomes.
- Module architecture-test result.
- Migration validation result.
- Concurrency-test result.
- End-to-end result.
- Known limitations and deferred scenarios.

