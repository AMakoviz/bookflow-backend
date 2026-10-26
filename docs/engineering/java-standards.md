# Java Engineering Standards

| Field | Value |
|---|---|
| Status | Accepted |
| Scope | BookFlow Spring Boot backend |

## 1. Baseline

- Use an actively supported Java LTS release.
- Use Spring Boot for service applications.
- Use Maven Wrapper for reproducible builds.
- The backend must remain independently buildable, testable, and containerizable.
- Use PostgreSQL in production-like and integration environments.
- Use Flyway for every persisted schema change.

## 2. Internal structure

The backend uses conventional package-by-layer organization:

```text
com.bookflow/
  config/
  controller/
  handler/
  filter/
  dto/request/
  dto/response/
  mapper/
  service/
    impl/
  repository/
  entity/
  exception/
  validation/
```

Rules:

- Controllers translate HTTP requests and delegate to services.
- Handlers translate exceptions to HTTP problem DTOs; servlet filters contain cross-cutting request concerns.
- Public service contracts are interfaces in `service`.
- Implementations reside in `service.impl`, use the `ServiceImpl` suffix, coordinate use cases, business validation, and transactions, and are the only service types annotated with `@Service`.
- Controllers and other consumers depend on service interfaces rather than implementation classes.
- Repositories contain Spring Data access only.
- JPA entities remain inside the entity layer and are mapped to response DTOs.
- Request and response DTOs contain no persistence annotations.
- Mappers are MapStruct interfaces, use the shared `config.MapStructConfiguration`, and contain transformation logic only. Hand-written default methods are limited to conversions MapStruct cannot express directly.
- Validation classes contain reusable business rules and do not depend on HTTP or persistence.
- Controllers must never call repositories directly.

These layer rules are protected with ArchUnit tests.

## 3. Java conventions

- Prefer immutable value objects and records where appropriate.
- Use constructor injection; field injection is prohibited.
- Every hand-written Spring component with required dependencies must declare them as `private final` fields and use Lombok `@RequiredArgsConstructor`. Generated MapStruct components are excluded. Do not hand-write dependency-injection constructors.
- Import every referenced type explicitly. Wildcard imports such as `java.util.*` and wildcard static imports are prohibited.
- Configure IDE import optimization so it never collapses explicit imports into a wildcard import.
- Avoid static mutable state.
- Do not expose persistence entities through HTTP APIs.
- Use UUIDs for externally visible identifiers.
- Use `Instant` or offset-aware types at application boundaries; do not use server-local time implicitly.
- Validate invariants in the domain or application boundary, not only in controllers.
- Transactions begin at application use cases, not in controllers.
- Use Lombok for dependency-injection constructors. Do not use generated constructors where a constructor itself expresses domain validation or creation behavior.
- Use MapStruct's stable release for compile-time entity-to-DTO mapping. Configure it as a Spring component with constructor injection and fail the build on unmapped targets.
- Do not catch broad exceptions unless translating them at a boundary and preserving the cause.

## 4. Database conventions

- Use lowercase `snake_case` names.
- Name primary keys `id` and foreign/reference columns `<concept>_id`.
- Store audit timestamps as timezone-aware values.
- Name constraints explicitly so violations can be translated reliably.
- Do not edit an applied migration; add a new migration.
- Do not use an in-memory database as a substitute for PostgreSQL behavior.
- Map JPA associations with `FetchType.LAZY` by default. Use `@EntityGraph` or an explicit fetch join when a use case needs related data; `EAGER` requires a documented, measured justification.
- Use Lombok class-level `@Getter` and `@Setter` for JPA entity accessors. Keep business and behavior-specific methods explicit rather than generating them.

## 5. API implementation

- Keep transport DTOs separate from JPA entities.
- Use Jackson through Spring MVC for JSON request deserialization and response serialization.
- Controllers return response DTOs directly. Use `@ResponseStatus` for a fixed non-`200` status and set required headers explicitly; do not use `ResponseEntity` as the default DTO wrapper.
- Keep JSON behavior in `application.yml`; do not instantiate or configure ad hoc `ObjectMapper` instances in controllers.
- Validate request shape at the web boundary and business invariants in the application/domain layer.
- Return stable problem responses following `api-conventions.md`.
- Propagate correlation IDs through outgoing HTTP calls.
- Generate or validate API documentation as part of the build.

## 6. Quality gates

The build will enforce:

- Compilation with warnings treated intentionally.
- Deterministic formatting through Spotless.
- Static rules through Checkstyle or an equivalent configured checker, including `AvoidStarImport` for regular and static imports.
- Architecture constraints through ArchUnit.
- Unit and integration tests.
- Migration validation.

## 7. Application configuration

- Use `application.yml` as the canonical Spring Boot configuration file.
- Keep related settings grouped hierarchically and retain environment-variable overrides for credentials and deployment-specific values.
- Do not add a parallel `application.properties` file.

