# Frontend Engineering Standards

| Field | Value |
|---|---|
| Status | Accepted |
| Scope | BookFlow web application |

## 1. Baseline

- Use React with TypeScript.
- Enable TypeScript strict mode.
- Use a reproducible package-manager lockfile.
- Keep API access behind a typed client boundary.
- Use ESLint and Prettier as blocking checks.

## 2. Structure

Organize application code by product feature:

```text
src/
  app/
  features/availability/
  features/booking/
  features/my-bookings/
  shared/api/
  shared/ui/
  shared/lib/
```

Rules:

- Features may depend on `shared`, but `shared` must not depend on features.
- Components do not call `fetch` directly; requests go through the typed API client.
- Backend DTOs are not duplicated manually when they can be generated from OpenAPI.
- Booking business invariants remain authoritative on the backend.
- UI validation improves feedback but never replaces backend validation.

## 3. UI behavior

- Every remote state has explicit loading, empty, success, and error representations.
- A booking conflict is presented as a recoverable domain outcome, not a generic failure.
- Forms remain keyboard accessible and provide associated labels and validation messages.
- Dates displayed to the user use the organization timezone and clearly identify it where ambiguity is possible.
- Do not hide the lack of authentication in the demonstration build.

## 4. Testing

- Unit-test non-trivial presentation and transformation logic.
- Component-test the booking form and conflict state.
- Keep one end-to-end test for the approved vertical slice.
- Use stable selectors based on roles, labels, or dedicated test identifiers only when semantic selectors are insufficient.

