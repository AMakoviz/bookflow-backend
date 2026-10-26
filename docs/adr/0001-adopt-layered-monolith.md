# ADR-0001: Adopt a Classic Layered Monolith

| Field | Value |
|---|---|
| Status | Accepted |
| Date | 2026-10-25 |
| Decision owners | Product owner and implementation agent |

## Context

BookFlow is a portfolio project with one Spring Boot backend and one cohesive booking workflow. The initial implementation used business packages with hexagonal ports and adapters. During implementation review, the project owner chose a conventional Java/Spring structure because it is easier to navigate and makes common backend competencies immediately recognizable.

The project still has no independent teams, scaling profiles, release cadences, security zones, or operational requirements that justify distributed deployment.

## Decision

Use one Spring Boot deployable organized as a classic layered monolith.

The top-level Java packages are technical layers:

- `config`;
- `controller`;
- `handler`;
- `filter`;
- `dto.request` and `dto.response`;
- `mapper`;
- `service`;
- `service.impl`;
- `repository`;
- `entity`;
- `exception`;
- `validation`.

Controllers depend on service interfaces and MapStruct mapper interfaces. Exception handlers and servlet filters are separate from endpoint controllers. Implementations under `service.impl` own use-case orchestration and transactions and access data through Spring Data repositories. JPA entities remain internal, use lazy associations by default, and are converted to transport DTOs by generated MapStruct implementations. Spring MVC and Jackson deserialize request DTOs and serialize directly returned response DTOs. Hand-written Spring components with required dependencies use final fields and Lombok `@RequiredArgsConstructor`. Validation rules remain independent of HTTP and persistence concerns.

Use one PostgreSQL database with Flyway migrations, referential integrity, and the database-enforced booking-exclusivity constraint. Keep the React/TypeScript frontend as a separate deployable artifact.

## Alternatives considered

### Package by business capability with hexagonal boundaries

Rejected for this project because the additional ports, adapters, and nested packages make the small codebase harder to scan during portfolio review. It remains a valid architecture for systems where replaceable adapters or strong bounded-context isolation justify the extra structure.

### Multiple microservices

Rejected because Resource and Booking have no independent scaling or release requirements, and the booking workflow benefits from one database transaction and direct referential integrity.

### Unstructured monolith

Rejected because controllers, persistence, and business logic still require explicit dependency direction and automated architecture checks.

## Consequences

Positive:

- Familiar structure for Java/Spring reviewers.
- Less infrastructure indirection than the previous ports-and-adapters structure.
- Explicit service contracts and generated mapper implementations make enterprise conventions visible to reviewers.
- Each technical responsibility has a predictable location.
- Strong transactions and referential integrity remain simple.

Negative:

- Business concepts are spread across several technical packages.
- The structure may become harder to navigate as the number of features grows.
- Strong bounded-context isolation is no longer represented by package boundaries.
- Future extraction or package-by-feature migration will require deliberate refactoring.

## Validation

- ArchUnit enforces allowed dependencies between technical layers.
- Controllers cannot call repositories directly.
- DTOs and validation code remain independent of persistence.
- ArchUnit verifies that controllers use service contracts and that implementations remain under `service.impl`.
- MapStruct fails compilation when a response target is left unmapped.
- The complete backend remains one independently buildable and deployable Spring Boot application.
