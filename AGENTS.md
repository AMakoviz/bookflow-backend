# BookFlow Agent Instructions

These instructions apply to the entire repository.

## Delivery workflow

1. Treat approved Discovery, architecture documents, ADRs, and SDDs as the source of truth.
2. Do not implement a feature until its SDD status is `Accepted`.
3. If implementation reveals a material product or architecture change, update the relevant specification or ADR and request human approval before continuing that change.
4. Keep `progress.md` current at meaningful checkpoints.
5. A feature is not complete until its Evidence Bundle is present and all required checks pass.

## Architecture boundaries

- Preserve the boundaries defined in `docs/architecture.md`.
- Preserve the classic technical layers: controller, DTO, mapper, service, repository, entity, exception, validation, handler, filter, and config.
- Controllers call services and mappers, never repositories.
- Controllers accept request DTOs and return response DTOs directly. Use annotations for fixed HTTP statuses and set required headers explicitly; do not wrap ordinary DTO responses in `ResponseEntity`.
- Service contracts live in `service`; their Spring implementations live in `service.impl`, use the `ServiceImpl` suffix, and own business orchestration and transaction boundaries.
- Controllers and other consumers depend on service interfaces, never on `service.impl` classes.
- Repositories contain data-access concerns only.
- JPA entities must not cross the HTTP boundary; controllers expose response DTOs.
- Mappers are MapStruct interfaces using the shared `config.MapStructConfiguration`. They transform data only and must not query repositories.
- Validation code must remain independent of controllers and persistence.
- Hand-written Spring components with required dependencies use final fields and Lombok `@RequiredArgsConstructor`; generated MapStruct components are excluded. Do not hand-write dependency-injection constructors.
- JPA associations are `LAZY` by default. Load required relations explicitly with an entity graph or fetch join for the use case.
- JPA entity accessors use Lombok class-level `@Getter` and `@Setter`; keep behavior-specific methods explicit.
- Jackson is the JSON serializer and deserializer at the HTTP boundary. Never serialize JPA entities directly.
- Introduce a separate deployable or asynchronous infrastructure only when an accepted feature and ADR justify it.
- Do not place domain logic in controllers, application bootstrap code, or frontend code.

## Change discipline

- Keep changes scoped to the active SDD.
- Add database changes through versioned Flyway migrations.
- Keep public and internal HTTP contracts explicit and versioned.
- Never commit secrets or local credentials.
- Preserve unrelated user changes.

## Verification

- Run the smallest relevant checks during iteration and the full required suite before completion.
- Use PostgreSQL-backed integration tests for PostgreSQL-specific behavior.
- Do not replace concurrency evidence with mocks or in-memory databases.
- Record commands, outcomes, limitations, and deferred work in the active Evidence Bundle.

## Repository guidance

General engineering conventions live under `docs/engineering/`. More specific `AGENTS.md` files may be added inside the backend or frontend when local rules are needed; they should not duplicate this file.

