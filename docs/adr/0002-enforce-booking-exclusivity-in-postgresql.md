# ADR-0002: Enforce Booking Exclusivity in PostgreSQL

| Field | Value |
|---|---|
| Status | Accepted |
| Date | 2026-10-25 |
| Scope | Booking service and persistence layers |

## Context

Availability search and an application-level overlap query cannot guarantee exclusivity. Two requests can observe the same room as available and attempt to insert overlapping bookings concurrently.

BookFlow requires the database to remain the final authority for the invariant: one room cannot have overlapping confirmed bookings.

## Decision

The booking persistence layer stores start and end as PostgreSQL `timestamptz` values and treats intervals as half-open: `[start, end)`.

The database migration will enable the required GiST support and create an exclusion constraint equivalent to:

```sql
EXCLUDE USING gist (
    room_id WITH =,
    tstzrange(start_at, end_at, '[)') WITH &&
)
WHERE (status = 'CONFIRMED')
```

The application performs an overlap query before insertion to return a helpful response in the common case. The exclusion constraint handles races and is authoritative.

A constraint violation is translated into a stable `409 Conflict` API response. Under a precisely simultaneous GiST exclusion check, PostgreSQL can abort one contender with deadlock SQLSTATE `40P01` instead of exclusion SQLSTATE `23P01`; because this occurs while flushing the booking insert, that losing contender is translated to the same booking conflict while the database still commits only one row.

## Alternatives considered

### Application-level check only

Rejected because it has a time-of-check/time-of-use race and cannot protect concurrent requests.

### Pessimistic locking of a room row

Rejected as the primary mechanism because it serializes booking attempts through explicit application locking and makes correctness depend on every write path acquiring the lock. The exclusion constraint expresses the invariant directly and protects every insertion path.

### Serializable transactions with retry

Valid but more operationally complex for this invariant. It may cause broader retries and makes the rule less explicit than a database constraint.

### Distributed lock

Rejected because it introduces another system and failure mode for an invariant that one PostgreSQL database can enforce directly.

## Consequences

Positive:

- Correct behavior under concurrent requests.
- The invariant is visible in the database schema.
- Adjacent intervals remain valid.
- No distributed coordination is required.

Negative:

- The implementation is PostgreSQL-specific.
- Integration tests must run against PostgreSQL rather than an in-memory substitute.
- Error translation depends on identifying the named constraint or PostgreSQL's transaction-abort code for the simultaneous exclusion-check case.

## Validation

The Evidence Bundle must include an integration test that starts two concurrent transactions attempting to create overlapping bookings for the same room. Exactly one request must succeed and exactly one must receive a conflict result.

