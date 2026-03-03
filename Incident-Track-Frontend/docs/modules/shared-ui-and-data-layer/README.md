# Shared UI And Data Layer

This guide covers the reusable infrastructure that most pages depend on: shared API plumbing, notifications state, common UI primitives, and shared type definitions.

## Core files

- [constants.ts](../../../src/config/constants.ts)
- [axiosInstance.ts](../../../src/lib/axios/axiosInstance.ts)
- [interceptors.ts](../../../src/lib/axios/interceptors.ts)
- [NotificationContext.tsx](../../../src/context/NotificationContext.tsx)
- [features/notifications/api.ts](../../../src/features/notifications/api.ts)
- [features/notifications/types.ts](../../../src/features/notifications/types.ts)
- [NotificationsPage.tsx](../../../src/pages/notifications/NotificationsPage.tsx)
- [components/common](../../../src/components/common)
- [components/charts/ReportCharts.tsx](../../../src/components/charts/ReportCharts.tsx)
- [types/common.ts](../../../src/types/common.ts)
- [types/pagination.ts](../../../src/types/pagination.ts)

## API plumbing

`axiosInstance.ts` creates the shared Axios client using the configured backend base URL.

`interceptors.ts` adds two important behaviors:

- inject the JWT token into every request when it exists
- trigger the caller's unauthorized handler on `401`

That means most feature API files can stay simple and focus only on endpoint shape.

## Notification state

`NotificationContext.tsx` is the global notification store. It is one of the most important shared files in the frontend because it combines:

- initial REST loading of the user's inbox
- SSE subscription to `/notifications/subscribe`
- automatic reconnect with backoff
- local read-state updates
- derived unread counts used by multiple UI surfaces

The same state powers:

- the bell dropdown in `Header.tsx`
- the badge in `Sidebar.tsx`
- the full inbox screen in `NotificationsPage.tsx`

## Notifications page

`NotificationsPage.tsx` is a good example of "global data + local view state":

- the notifications come from context
- the page adds local tabs for `ALL`, `UNREAD`, and `READ`
- unread items can be marked read inline

If notification behavior seems wrong in the UI, the bug is usually either in this page or in `NotificationContext.tsx`.

## Common UI primitives

The `components/common` folder contains the reusable building blocks used across most route pages:

- `TablePrimitives.tsx`
  Shared table row/cell styling.
- `SortableHeader.tsx`
  Standard sortable table headers.
- `Pagination.tsx`
  Shared pager.
- `StatusBadge.tsx`, `PriorityBadge.tsx`, `TaskStatusBadge.tsx`
  Domain-specific badge rendering.
- `ModalWindow.tsx`, `ProfilePanel.tsx`, `Select.tsx`, `ComboBox.tsx`
  Reusable interaction components used by larger pages.

This folder is the reason many pages in the app can stay focused on data and workflow rather than repeating presentation code.

## Shared types

- `types/pagination.ts`
  Shared paging request and response shapes.
- `types/common.ts`
  Shared domain helpers used across multiple features.

The feature folders then add their own domain-specific DTO types on top of these shared base types.
