# Evidence Bundle: BF-SDD-001

| Field | Value |
|---|---|
| Status | Ready for human acceptance |
| SDD | [`../../specs/001-create-booking.md`](../../specs/001-create-booking.md) |
| Architecture | [`../../docs/architecture.md`](../../docs/architecture.md) |
| Decisions | [`ADR-0001`](../../docs/adr/0001-adopt-layered-monolith.md), [`ADR-0002`](../../docs/adr/0002-enforce-booking-exclusivity-in-postgresql.md) |

## Verification record

| Check | Command | Result |
|---|---|---|
| Backend full quality gate | `backend\\mvnw.cmd verify` | Passed after the MapStruct and service-contract refactor: 28 tests, 0 failures, 0 errors, 0 skipped; Spotless and Checkstyle passed |
| PostgreSQL race stress check | focused concurrency test, repeated 3 times | Passed: each run persisted one booking and returned one domain conflict |
| HTTP and generated OpenAPI integration | included in Maven `verify` | Passed: 8 HTTP/OpenAPI tests, including Jackson serialization/deserialization, strict unknown-field rejection, invalid interval, room, and employee outcomes |
| Architecture boundaries | included in Maven `verify` | Passed: 9 ArchUnit tests covering controller, handler, filter, DTO, repository, entity, validation, service contracts and implementations, mapper interfaces, and the no-`ResponseEntity` DTO rule |
| Frontend generated API types | `pnpm api:generate` | Passed from [`../../docs/api/openapi.yaml`](../../docs/api/openapi.yaml) |
| Frontend lint and formatting | `pnpm lint` | Passed |
| Frontend unit and component tests | `pnpm test` | Passed: 7 tests across 3 files, including form, loading, empty, results, and selection states |
| Frontend production build | `pnpm build` | Passed: TypeScript compilation and Vite production bundle |
| Docker Compose smoke test | `docker compose config --quiet` and `docker compose up -d --build --wait` | Passed: PostgreSQL, backend, and frontend healthy |
| Browser end-to-end journey | `pnpm exec playwright test` | Passed: 1 Chrome journey against the Compose stack |

## Database and concurrency evidence

- Flyway created the `resource` and `booking` schemas in PostgreSQL 18 and loaded stable demonstration data.
- The named `booking_no_room_overlap` GiST exclusion constraint is the final authority for half-open interval overlap.
- The race test starts two simultaneous transactions for the same room and interval. PostgreSQL may reject the losing transaction with either exclusion SQLSTATE `23P01` or an exclusion-check deadlock `40P01`; `BookingService` translates both into `BookingConflictException` at the persistence boundary.
- Three consecutive focused race runs passed after verifying both PostgreSQL outcomes.

## Browser evidence

- [Successful booking and immediate list refresh](bookflow-happy-path.png)
- [Recoverable conflict with retained search input](bookflow-conflict.png)

Both screenshots were produced by the passing Playwright journey against the real nginx, Spring Boot, and PostgreSQL services and were visually inspected.

## Demonstration

Follow the one-command startup and demonstration path in the project [`README`](../../README.md). The verified local endpoints are:

- application: `http://localhost:3000`;
- backend readiness: `http://localhost:8080/actuator/health/readiness`;
- generated OpenAPI: `http://localhost:8080/api-docs`.

The local stack was left running and healthy. Bookings created by automated verification were removed afterward, leaving the demo employee with a clean schedule.

## Known intentional limitations

- The application uses one fixed demonstration employee and has no authentication.
- Rooms and employees are seed data; there is no administration UI.
- Cancellation, rescheduling, recurring bookings, and notifications remain outside BF-SDD-001.
- The stack is intended for local demonstration and is not hardened for public exposure.

The Java 25 test run reports a non-failing Mockito dynamic-agent deprecation warning from the current Spring test toolchain. It does not affect the verified behavior but should be revisited when that toolchain changes.

## Release-readiness statement

Implementation and required automated evidence for `BF-SDD-001` are complete. No required check is skipped. The feature is ready for the project owner's visual review and explicit release acceptance; public deployment remains outside the accepted scope.

The backend was subsequently refactored from capability-oriented hexagonal packages to the accepted classic technical layers (`controller`, `handler`, `filter`, `dto`, `mapper`, `service`, `repository`, `entity`, `exception`, and `validation`). The API contract, Flyway schema, PostgreSQL exclusion constraint, and browser behavior remained unchanged and the complete verification suite was renewed.

Review corrections added Lombok-generated dependency-injection constructors and entity accessors, explicit Jackson configuration through `application.yml`, direct DTO controller responses, separate `handler` and `filter` packages, and lazy JPA relations with an explicit booking-list entity graph. The `201 Created` status and `Location` response header remain compatible with the accepted API contract.

The enterprise-structure checkpoint introduced explicit `BookingService` and `RoomService` contracts, isolated Spring implementations in `service.impl`, and made controllers depend only on those contracts. Handwritten DTO conversion was replaced by MapStruct 1.6.3 mapper interfaces using Spring components, constructor injection, and build-time failure for unmapped target fields. ArchUnit now guards these structural decisions.

