# BookFlow Progress

Last updated: 2026-10-25

## Completed

- [x] Selected BookFlow product direction.
- [x] Defined the first vertical slice.
- [x] Approved Discovery `BF-DISC-001`.
- [x] Replaced the capability-oriented decomposition with a classic layered backend structure.
- [x] Recorded the layered-monolith architecture baseline.
- [x] Accepted ADR-0001 for the classic layered monolith.
- [x] Accepted ADR-0002 for PostgreSQL booking exclusivity.
- [x] Accepted the Java, frontend, API, and testing standards.
- [x] Confirmed Maven, React with TypeScript, and conventional Spring layers.
- [x] Approved feature specification `BF-SDD-001`.
- [x] Refactored the Java/Spring backend into the accepted classic layered structure.
- [x] Standardized constructor injection with Lombok `@RequiredArgsConstructor`.
- [x] Separated controllers, exception handlers, and servlet filters.
- [x] Made Jackson DTO serialization explicit, migrated configuration to YAML, and replaced eager JPA relations with explicit lazy fetch plans.
- [x] Replaced handwritten DTO mapping with MapStruct mappers and a shared strict mapper configuration.
- [x] Introduced service contracts with Spring implementations isolated under `service.impl`.
- [x] Enforced mapper and service boundaries with ArchUnit tests.
- [x] Applied Flyway migrations and PostgreSQL-enforced booking exclusivity.
- [x] Passed backend, frontend, architecture, contract, and concurrency checks.
- [x] Passed the complete Docker Compose and Chrome browser journey.
- [x] Completed the Evidence Bundle with inspected happy-path and conflict screenshots.

## Current checkpoint

The backend now follows the approved classic layered structure with explicit service contracts and generated MapStruct adapters. Backend, frontend, PostgreSQL concurrency, Compose, architecture, and browser checks have been renewed successfully. The implementation is ready for project-owner code review.

## Next actions

1. Project owner reviews the layered implementation.
2. Record any requested code-level corrections in this checkpoint.
3. Select the next BookFlow feature after review acceptance.

## Open questions

- None at this checkpoint. Material corrections discovered during SDD review must be resolved before implementation.

## Evidence pointer

[`evidence/001-create-booking/summary.md`](evidence/001-create-booking/summary.md)
