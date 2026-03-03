# Task / Incident / Urgent Change Summary

## What changed

### Backend

- Task-to-incident is now modeled as one-to-one in JPA.
  - `Task.incident` was changed from `@ManyToOne` to `@OneToOne`.
  - `incident_id` is marked unique at the mapping level.
  - Repository access for "task by incident" now uses a single task shape internally.

- Task creation flow was aligned with the one-to-one model.
  - The incident is explicitly saved as `IN_PROGRESS` before the task is persisted.
  - The created task still inherits its due date from the incident SLA deadline.
  - Existing service validation that blocks multiple tasks for the same incident remains in place.

- `isCritical` was renamed to `urgent` in the backend API model.
  - Request DTO now exposes `urgent`.
  - Response DTO now exposes `urgent`.
  - Repository/service methods were renamed to use `urgent`.
  - `IncidentResponseDTO` now also returns `slaDueAt`.

- Incident creation SLA logic was moved into the service layer.
  - The effective SLA deadline is now calculated from the category SLA value loaded from the database.
  - If `urgent = true`, the SLA window is halved using minutes (so odd-hour SLAs still work correctly).
  - Severity still becomes `CRITICAL` when the incident is marked urgent.
  - `Incident` entity no longer hardcodes urgent SLA handling in `@PrePersist`.

- Urgent filter endpoints were updated.
  - New primary endpoints: `/api/incidents/urgent` and `/api/incidents/urgent/paged`
  - Backward-compatible aliases for the old `/CRITICAL` routes are still accepted.
  - Request payloads still accept legacy `isCritical` through `@JsonAlias`.

- Notifications and compliance text were updated.
  - Manager alerts now use "Urgent incident" wording.
  - SLA breach reason text now refers to the effective SLA deadline.
  - No scheduler algorithm rewrite was needed because compliance already monitors `incident.slaDueAt`.

### Frontend

- Incident API/types were updated to use `urgent` instead of `isCritical`.
- Incident create screen now:
  - sends `urgent`
  - labels the checkbox as urgent
  - shows the effective SLA window, including the halved window when urgent is checked

- Incident list screen now:
  - uses the urgent endpoint
  - renames the checkbox filter to "Only Urgent"

- Incident detail screen now:
  - shows `Urgent`
  - shows `Base SLA`
  - shows `SLA Due At`

- Home page now counts and labels urgent incidents instead of critical incidents.

- Task create screen now filters out open incidents that already have a task, so the one-to-one rule is reflected in the UI and not only enforced by the backend.

- Notifications page now labels `CRITICAL_INCIDENT_ALERT` as "Urgent Alert" in the UI.

## Important rollout notes

### Things that were easy to miss

1. Existing incidents already stored in the database keep their current `sla_due_at` values.
   - The new halved-SLA behavior applies to newly created incidents.
   - If you want old urgent incidents recalculated, you need a separate backfill/update script.

2. The Java/API field is renamed to `urgent`, but the physical database column is still mapped to the existing `is_critical` column.
   - This was kept intentionally to avoid forcing a schema rename during this code change.
   - If you want the database column renamed too, do that with an explicit migration.

3. If your production schema is managed manually, add a real database unique constraint on `tasks.incident_id`.
   - The code now treats the relationship as one-to-one.
   - JPA mapping expresses that intent, but an existing database will only enforce it if the schema is updated.

4. External clients that consume the incident API should move to `urgent`.
   - Old request payloads using `isCritical` are still accepted.
   - Response payloads now return `urgent`.

## Verification done

- Frontend TypeScript check passed:
  - `npx tsc -p tsconfig.app.json --noEmit`

- Backend main sources compiled successfully:
  - `.\mvnw.cmd -q -DskipTests compile`

- Backend tests were not rerun as part of this change.
