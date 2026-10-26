# BookFlow Architecture

| Field | Value |
|---|---|
| Status | Approved baseline |
| Version | 0.5 |
| Date | 2026-10-25 |
| Related Discovery | [`discovery.md`](discovery.md) |
| Primary decision | [`ADR-0001`](adr/0001-adopt-layered-monolith.md) |
| Booking consistency | [`ADR-0002`](adr/0002-enforce-booking-exclusivity-in-postgresql.md) |

## 1. Architecture goals

BookFlow uses a classic layered monolith. The structure is intentionally familiar to Java/Spring developers and keeps technical responsibilities easy to locate while retaining one strong transaction boundary.

The architecture must:

- Separate HTTP, mapping, service, persistence, entity, validation, and configuration responsibilities.
- Keep controllers thin and free of business rules.
- Prevent overlapping confirmed bookings under concurrent requests.
- Keep DTOs separate from JPA entities.
- Expose an explicit, versioned browser-facing API.
- Remain runnable locally through Docker Compose.
- Keep infrastructure proportional to the approved feature scope.

## 2. System context

```text
Employee
   |
   v
React/TypeScript web application
   |
   v
BookFlow Spring Boot layered monolith
   |
   v
PostgreSQL
```

The browser communicates with one backend application. The backend is one deployable and uses one PostgreSQL database.

## 3. Deployable containers

### Web application

- React and TypeScript single-page application.
- Implements search, room selection, booking confirmation, and booking-list views.
- Uses generated or contract-checked API types.
- Contains no authoritative booking business rules.

### BookFlow backend

- One Spring Boot deployable application.
- Exposes the REST API and owns transaction boundaries.
- Uses conventional technical layers under `com.bookflow`.
- Contains health endpoints and cross-cutting configuration.

### PostgreSQL

- One application database.
- All schema changes are versioned with Flyway.
- Referential integrity and booking exclusivity are enforced by the database.

## 4. Backend package structure

```text
com.bookflow/
  config/       Spring and OpenAPI configuration
  controller/   HTTP endpoints
  handler/      exception-to-HTTP problem mapping
  filter/       cross-cutting servlet request processing
  dto/
    request/     inbound API payloads
    response/    outbound API payloads
  mapper/       MapStruct entity-to-DTO contracts
  service/      service interfaces
    impl/       Spring service implementations, transactions, and orchestration
  repository/   Spring Data persistence interfaces
  entity/       JPA entities and persisted enums
  exception/    application exceptions
  validation/   reusable booking rules and interval validation
```

Classes are named by business concept inside each technical layer, for example `BookingController`, `BookingService`, `BookingRepository`, `BookingEntity`, and `BookingMapper`.

## 5. Dependency rules

```text
controller --> service interface <-- service.impl --> repository --> entity
     |                                      |                         |
     v                                      v                         v
    dto                                 validation                LAZY relations
     ^                                                                ^
     |                                                                |
MapStruct mapper <-----------------------------------------------------+

handler --> dto + exception
filter  --> servlet infrastructure
```

Rules:

- Controllers call services and mappers, never repositories.
- Controllers return response DTOs directly; Jackson performs JSON serialization and deserialization.
- Exception handlers and servlet filters are separate from endpoint controllers.
- Service interfaces define use-case contracts; `service.impl` classes own orchestration, validation, and transaction boundaries.
- Controllers depend on service interfaces and never import implementations.
- Repositories contain persistence access only.
- JPA entities never cross the HTTP boundary.
- Request and response DTOs contain no persistence annotations.
- MapStruct mapper interfaces contain transformation declarations only and do not query repositories. The shared `config.MapStructConfiguration` enforces Spring components, constructor injection, and unmapped-target errors.
- Validation code does not depend on controllers, DTOs, repositories, or JPA.
- Hand-written Spring components with dependencies use final fields and Lombok `@RequiredArgsConstructor`; MapStruct owns its generated constructors.
- JPA relations are lazy by default and are fetched explicitly for use cases that need them.
- Dependency rules are protected with ArchUnit tests.

## 6. Data model

The database retains separate `resource` and `booking` schemas to make table purpose visible, but Java code is organized by technical layer rather than business module.

| Schema | Tables |
|---|---|
| `resource` | meeting rooms and room attributes |
| `booking` | employees and confirmed bookings |

Foreign keys and the PostgreSQL exclusion constraint remain authoritative.

## 7. Main request flows

### Search available rooms

1. `AvailabilityController` validates request shape and calls `BookingService`.
2. `BookingService` validates the interval through `BookingValidator`.
3. `RoomRepository` returns active rooms with sufficient capacity.
4. `BookingRepository` returns occupied room identifiers.
5. `RoomMapper` converts the available entities to response DTOs.

### Create booking

1. `BookingController` accepts `CreateBookingRequest`.
2. `BookingService` validates time, employee, room state, and capacity.
3. It performs an advisory overlap check and saves one confirmed `BookingEntity` in a transaction.
4. PostgreSQL remains the final authority against concurrent overlap.
5. `BookingMapper` creates the response DTO.

### List employee bookings

1. `BookingController` calls `BookingService` for the employee.
2. `BookingRepository` returns upcoming confirmed bookings ordered by start time.
3. `BookingMapper` produces response DTOs including room display data.

## 8. Consistency and time model

- Booking creation is one database transaction.
- Booking exclusivity is strongly consistent and PostgreSQL-specific.
- API timestamps use RFC 3339 with explicit offsets.
- Persistence uses `timestamptz`.
- Office rules use `Europe/Moscow`.
- Booking intervals are half-open: `[start, end)`.
- Adjacent bookings are allowed; overlapping bookings are rejected.

## 9. API and error model

- The browser-facing API is described with OpenAPI.
- Identifiers are UUIDs.
- Validation errors return `400 Bad Request` with a stable problem payload.
- Missing resources return `404 Not Found`.
- Booking overlap returns `409 Conflict`.
- DTOs, entities, and persistence concerns remain separate.
- Every request receives a correlation identifier.

## 10. Security and observability baseline

The first feature uses a seeded demonstration user and has no authentication. This limitation is visible in the UI and README. Database credentials are supplied through environment configuration and are never committed.

The backend provides structured logs, correlation IDs, and health/readiness endpoints. Authentication, authorization, centralized tracing, and public deployment require separate approved work.

## 11. Deployment topology

Docker Compose runs the web application, Spring Boot backend, and PostgreSQL. Frontend and backend remain independently buildable and testable outside Compose.

## 12. Planned evolution

New capabilities are added to the layered monolith unless an accepted ADR identifies a concrete reason for a separate deployable. If the codebase grows enough that technical packages become difficult to navigate, a future ADR may introduce bounded business modules.

## 13. Architecture fitness checks

- ArchUnit enforces controller, handler, filter, service, repository, entity, DTO, mapper, and validation dependency direction.
- Controllers cannot access repositories directly.
- DTOs cannot depend on repositories or entities.
- Flyway migrations validate against PostgreSQL.
- A concurrency test proves two overlapping requests cannot both succeed.
- Docker Compose and browser tests prove the complete user journey.
