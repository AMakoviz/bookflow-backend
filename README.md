# BookFlow

BookFlow is a portfolio-grade meeting-room booking application built through specification-driven development. A user searches by time and capacity, books an available room, and immediately sees the confirmed reservation. PostgreSQL remains the final authority against overlapping bookings, including concurrent requests.

This repository contains the Java backend, API contract, architecture decisions, SDD, and verification evidence. The React client lives in [bookflow-frontend](https://github.com/AMakoviz/bookflow-frontend). Both repositories are tracked through the BookFlow GitHub Project.

The application contains no AI product feature. AI is used as the implementation agent behind an approved SDD; product intent, architecture checkpoints, and release acceptance stay human-owned.

## What this slice demonstrates

- Java 25 and Spring Boot 4.1.
- A classic layered monolith with ArchUnit-enforced dependency direction.
- Conventional `controller`, `handler`, `filter`, `service`, `service.impl`, `repository`, `entity`, `dto`, and `mapper` packages.
- PostgreSQL schemas, Flyway migrations, referential integrity, and a GiST exclusion constraint.
- Transactional conflict handling with a real concurrent integration test.
- A React/TypeScript UI with generated OpenAPI types and explicit remote states.
- Unit, HTTP, PostgreSQL/Testcontainers, architecture, frontend, and browser tests.
- Reproducible local startup with Docker Compose.

## Architecture at a glance

```text
React / TypeScript
        |
        v
Spring Boot API composition
        |
        v
controller -> service -> repository -> entity
     |            |                    |
     v            v                    v
    dto       validation           PostgreSQL
     ^                                 |
     +------------- mapper <------------+
```

Controllers return DTOs serialized by Jackson and depend on service interfaces; handlers map exceptions, filters implement cross-cutting HTTP concerns, `service.impl` classes own business orchestration and transactions, repositories own data access, MapStruct generates mapper implementations, and JPA entities remain internal with lazy relations. The application is one backend deployable because the workflow benefits from one strong consistency boundary and has no independent scaling or release driver.

## Run locally

Requirements: Docker Desktop with Docker Compose.

```powershell
Copy-Item .env.example .env
docker compose up --build --wait
```

Open [http://localhost:3000](http://localhost:3000). The backend readiness endpoint is [http://localhost:8080/actuator/health/readiness](http://localhost:8080/actuator/health/readiness), and the generated OpenAPI document is [http://localhost:8080/api-docs](http://localhost:8080/api-docs).

The UI clearly identifies its fixed `Demo Employee`; authentication is deliberately outside the first feature.

Stop the stack with:

```powershell
docker compose down
```

Add `--volumes` only when you intentionally want to delete the local BookFlow database.

## Demonstration path

1. Keep the default date and 10:00-11:00 interval, or choose another valid future 30-minute interval.
2. Enter the participant count and search.
3. Choose a returned room and create the booking.
4. Observe the confirmation and the immediate update in `Мои бронирования`.
5. Repeating the same room and interval produces a recoverable conflict instead of a duplicate booking.

Business defaults are `Europe/Moscow`, 08:00-22:00 working hours, 30-minute increments, 30-minute minimum duration, 4-hour maximum duration, and a 30-day booking horizon.

## Development checks

### IntelliJ IDEA

Open the repository root and load `pom.xml` as a Maven project. Select the installed Java 25 JDK for the project SDK.

If imports appear as `import ...`, IntelliJ has only folded the import block in the editor. Expand it from the gutter, or disable `Settings | Editor | General | Code Folding | Java | Imports`. The build independently prohibits actual wildcard imports such as `java.util.*` through both Spotless and Checkstyle.

Backend checks require Java 25 and Docker because PostgreSQL integration tests use Testcontainers:

```powershell
.\mvnw.cmd verify
```

Frontend checks use pnpm 11 in the separate [bookflow-frontend](https://github.com/AMakoviz/bookflow-frontend) repository:

```powershell
pnpm install --frozen-lockfile
pnpm api:generate
pnpm lint
pnpm test
pnpm build
```

With the Compose stack running, run the browser journey with a locally installed Chrome browser:

```powershell
pnpm e2e
```

If Maven reports an invalid `JAVA_HOME`, point it at the installed Java 25 JDK for the current shell before invoking the wrapper.

## Source-of-truth documents

- [Discovery](docs/discovery.md)
- [Architecture](docs/architecture.md)
- [Accepted feature SDD](specs/001-create-booking.md)
- [Engineering standards](docs/engineering/java-standards.md)
- [Evidence Bundle](evidence/001-create-booking/summary.md)

Material product or architecture changes start in these documents before implementation changes.
