# BookFlow: Discovery

| Field | Value |
|---|---|
| Discovery ID | `BF-DISC-001` |
| Status | Approved |
| Date | 2026-10-25 |
| Approved | 2026-10-25 |
| Product owner / final approver | Project owner |
| Discovery facilitator | AI assistant |
| Delivery mode | Specification-driven development |

## 1. Opportunity

Employees of a small company need a quick way to find and reserve a meeting room for a specific time. When availability is maintained informally or in disconnected calendars, employees spend time checking rooms manually and can create conflicting reservations.

BookFlow should provide one reliable booking flow: enter the desired interval and number of participants, choose an available room, and receive a confirmed reservation. The system must prevent double booking even when two users attempt to reserve the same room concurrently.

## 2. Target user

### Primary user

An employee who needs a meeting room for an upcoming meeting.

### Future users

- Office administrator managing rooms and their availability.
- Team lead viewing room usage.
- Organization administrator managing users and policies.

Future users are outside the first feature scope.

## 3. Current problem

- Room availability is not visible in one place.
- Booking requires manual coordination.
- Two employees can attempt to reserve the same room and time.
- The user does not receive a clear, durable confirmation of the reservation.

## 4. Desired future state

An employee can complete a booking without assistance:

1. Enter date, start time, end time, and participant count.
2. See rooms that are available for the entire requested interval and have sufficient capacity.
3. Select a room.
4. Create a reservation.
5. See an explicit confirmation and the reservation in the employee's booking list.

If the room becomes unavailable before confirmation, BookFlow rejects the request as a conflict and invites the employee to choose another room or interval.

## 5. Internal press release

### BookFlow removes the coordination cost from meeting-room booking

Employees can now find and reserve an available meeting room in less than one minute. BookFlow shows only rooms that fit the requested time and capacity and protects every reservation from scheduling conflicts. Employees receive an immediate confirmation, while the company gets a reliable source of booking data that can later support administration, notifications, and utilization analytics.

## 6. FAQ

### Who is the first version for?

Employees of one small company using a shared set of meeting rooms.

### Does a booking require administrator approval?

The proposed first version confirms a valid booking immediately. Approval workflows are outside the first feature.

### How does the system prevent double booking?

Availability is checked when rooms are searched and checked again when the booking is created. The database remains the final authority and must reject overlapping reservations under concurrent requests.

### Does the first version include registration and authentication?

No. A demonstration user is created with database migrations. Authentication will be delivered as a separate feature so it does not obscure the booking-domain slice.

### Can users cancel or modify bookings?

Not in the first feature. Cancellation and rescheduling will be separate SDDs.

### Does BookFlow use AI in the product?

No. AI is part of the delivery process: the human owns product intent and acceptance, while the AI agent implements an approved SDD and produces verification evidence.

## 7. Outcome hypothesis

### Product outcome

A user can create a valid meeting-room reservation in less than 60 seconds without assistance.

### Leading indicators

- At least 90% of scripted user journeys reach a confirmed booking without an error.
- 100% of known overlapping-booking scenarios are rejected.
- A confirmed booking is visible immediately in the user's booking list.

### Validation period

The initial hypothesis is checked against the acceptance and end-to-end scenarios before the first demonstration. If the application is later used by real users, booking completion time and failure rate will be measured over the first 14 days.

### Fallback condition

If fewer than 90% of scripted journeys complete successfully, the first release is not accepted. Failures are classified as UX, validation, availability-query, or concurrency defects; the specification is updated before implementation is corrected.

## 8. Delivery approach decision

BookFlow is a greenfield product, so the first increment is not an adaptation of an existing workflow or codebase. The product is intentionally designed around self-service booking and transactional conflict prevention.

The delivery task is classified as AI-native development through an SDD:

- The human owns the intent, scope, product decisions, and final acceptance.
- The AI agent may design and implement within the approved specification.
- Architectural or product-scope changes require human review.
- Deployment and release remain human-approved.

## 9. First vertical slice

### In scope

- PostgreSQL-backed users, rooms, and bookings.
- Seed data for one demonstration user and several rooms.
- Search for rooms available for a requested time interval and participant count.
- Creation of a booking for an available room.
- Transaction-safe prevention of overlapping bookings.
- Display of the demonstration user's bookings.
- A browser interface for the complete journey.
- Automated unit, integration, concurrency, and end-to-end checks appropriate to the slice.
- Local startup through Docker Compose.

### Out of scope

- Registration, login, JWT, and external identity providers.
- Roles and administrative UI.
- Room creation or editing through the UI.
- Cancellation and rescheduling.
- Recurring bookings.
- Approval workflows.
- Notifications and calendar integrations.
- Payments, analytics, and multi-tenancy.
- AI-powered product features.

## 10. Product constraints

- A booking must have a start time earlier than its end time.
- A booking cannot be created for a past interval.
- A room must accommodate the requested participant count.
- A room cannot have overlapping active bookings.
- A booking is either confirmed or rejected; the first feature has no provisional state.
- Availability shown to the user is advisory until booking creation succeeds.
- All persisted schema changes must be delivered through versioned migrations.

## 11. Human-in-the-loop decision map

| Checkpoint | Owner | Evidence | Blocking decision |
|---|---|---|---|
| Discovery approval | Product owner | This document | SDD cannot be finalized before approval |
| SDD approval | Product owner | Feature specification and acceptance scenarios | Implementation cannot start before approval |
| Architecture review | Product owner with AI explanation | Module boundaries, data model, concurrency strategy | Material architecture changes require approval |
| Release readiness | Product owner | Build, tests, evidence bundle, demo instructions | First release requires explicit acceptance |
| Outcome check | Product owner | Scripted journey results and measured timings | Failed hypothesis returns the work to specification |

## 12. Initial evidence expectations

The first feature is complete only when the evidence bundle contains:

- A link or reference to the approved SDD.
- Database migration history.
- Unit-test results for booking rules.
- PostgreSQL integration-test results.
- Evidence that concurrent overlapping requests cannot both succeed.
- Frontend build and end-to-end journey result.
- API contract generated or verified from the implementation.
- A concise list of assumptions, limitations, and deferred features.
- Local run and demonstration instructions.

## 13. Approved product defaults

These values were accepted with the Discovery approval:

| Decision | Proposed default |
|---|---|
| Organization type | One small company |
| Bookable resource | Meeting room |
| Office timezone | `Europe/Moscow` |
| Minimum booking duration | 30 minutes |
| Time increment | 30 minutes |
| Maximum booking duration | 4 hours |
| Booking horizon | 30 days in advance |
| Working hours | 08:00-22:00 |
| Confirmation | Immediate when all rules pass |
| Demonstration identity | One seeded employee |

## 14. Discovery approval record

The product owner approved this Discovery on 2026-10-25, including:

1. The opportunity and target user.
2. The first vertical-slice boundary.
3. The product defaults in section 13.
4. The outcome hypothesis.
5. Creation of `specs/001-create-booking.md` after the architecture checkpoint.

The application architecture is defined by [`ADR-0001`](adr/0001-adopt-layered-monolith.md) and the booking consistency strategy by [`ADR-0002`](adr/0002-enforce-booking-exclusivity-in-postgresql.md). Both decisions are accepted.
