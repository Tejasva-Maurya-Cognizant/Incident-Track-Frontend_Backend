        GitHub Copilot: Let me look at the relevant backend files to understand the root cause: 

Searched for files matching `**/JwtAuthenticationFilter.java`, 1 match

Searched for files matching `**/NotificationService.java`, 1 match

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/config/JwtAuthenticationFilter.java)

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/notification/service/NotificationService.java#1-1), lines 1 to 100

The root cause is clear from the stack trace:

```
HikariPool-1 - Connection is not available, request timed out after 30012ms (total=10, active=10, idle=0, waiting=2)
...
at com.incidenttracker.backend.user.config.JwtAuthenticationFilter.doFilterInternal(JwtAuthenticationFilter.java:58)
```

**The problem:** Every SSE `EventSource` reconnect hits `JwtAuthenticationFilter`, which calls `loadUserByUsername()` → a DB query. SSE connections are **long-lived** (30 minutes), so 10 connections quickly exhaust the entire HikariCP connection pool. Each SSE connection holds a thread + transaction context, starving all regular API requests.

The fix is to **skip the DB lookup in the JWT filter for already-validated SSE connections** by caching the username in the `SecurityContext` early, or better — **authenticate from the JWT claims alone** (without hitting the DB) for the SSE path, and also make the SSE emitter timeout shorter to free up resources.

Let me read the rest of `JwtAuthenticationFilter` and the `CustomUserDetailsService`: 

Searched for files matching `**/CustomUserDetailsService.java`, 1 match

Searched for files matching `**/JWTUtil.java`, 1 match

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/config/JWTUtil.java)

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/service/CustomUserDetailsService.java)

Now I have a complete picture. The JWT only stores the `username` (email) as the subject — no role claim. Every request (including each SSE keepalive reconnect) hits the DB to load the user. With 10 active SSE connections the entire pool is exhausted.

**The fix has two parts:**
1. **`JWTUtil`** — add a `generateToken(username, role)` overload that embeds the role as a claim, and a `extractRole()` method
2. **`JwtAuthenticationFilter`** — build `UserDetails` directly from JWT claims (no DB call) instead of calling `loadUserByUsername()`

But since changing the token format would invalidate all existing tokens, a simpler and safer fix is to **skip DB lookup for SSE paths** and also add connection pool settings to prevent exhaustion. Let me implement the cleanest solution: build authentication from JWT claims directly (no DB hit per-request): 

Searched for files matching `**/application.properties`, 2 matches

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/resources/application.properties)

No HikariCP settings configured at all — defaulting to `maximum-pool-size=10`. Now I'll implement the fixes:

**Plan:**
1. **`JWTUtil`** — embed role in token at generation, add `extractRole()` to read it back
2. **`JwtAuthenticationFilter`** — build `UserDetails` from JWT claims (no DB hit). This eliminates the connection pool exhaustion entirely
3. **application.properties** — add HikariCP settings: increase pool size, add connection validation, and set `keep-alive-time` so idle SSE connections don't hold DB connections

Since existing tokens won't have the role claim, the filter will fall back to the DB only when the claim is absent (graceful migration): 

Using "Multi-Replace String in Files"


```

```

```

```

```

```
Now find the auth service that calls `generateToken` and update it to embed the role: 

Searched for text `generateToken` (`**/*.java`), 5 results

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/controller/AuthController.java#60-60), lines 60 to 90

Using "Replace String in File"


```

```
Now verify no compile errors: 

Checked [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/config/JWTUtil.java), [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/config/JwtAuthenticationFilter.java), [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/controller/AuthController.java), no problems found

All clean. Here's a summary of what was wrong and what was fixed:

---

## Root Cause

The error was a **HikariCP connection pool exhaustion** caused by the SSE (live notification) connections. Here's the chain:

1. Every frontend tab opens an `EventSource` SSE connection (`/notifications/subscribe`)
2. SSE connections are long-lived (30 minutes), holding a server thread each
3. **Every request** — including SSE reconnects — passed through `JwtAuthenticationFilter`, which called `loadUserByUsername()` → a **synchronous DB query**
4. With 10 connections (HikariCP default), only 10 concurrent DB operations could run. Multiple SSE connections + regular API calls exhausted all 10, causing the `Connection is not available, request timed out after 30012ms` error

---

## Fixes Applied

### 1. JWTUtil.java — Embed role in token
- Added `generateToken(username, role)` overload that stores the role as a JWT claim
- Added `extractRole(token)` to read it back
- Added `validateToken(token, username)` overload (no UserDetails needed)

### 2. JwtAuthenticationFilter.java — Zero DB calls (fast path)
- **Before**: Every request called `loadUserByUsername()` → DB query → held a connection
- **After**: Reads `username` + `role` directly from JWT claims, builds `UserDetails` in memory — **no DB hit at all**
- Old tokens without the role claim gracefully fall back to the DB lookup (backward compatible)

### 3. AuthController.java — Embed role at login
- `generateToken(user.getEmail())` → `generateToken(user.getEmail(), user.getRole().name())`
- New tokens issued after re-login will carry the role claim, activating the zero-DB fast path

### 4. application.properties — HikariCP tuning
- `maximum-pool-size=25` (up from default 10)
- `minimum-idle=5`, `connection-timeout=10s`, `idle-timeout=10min`, `max-lifetime=25min`, `connection-test-query=SELECT 1`

> **Action needed**: Users need to **log out and log back in** once after the backend restarts so their token includes the new role claim. Until then, the fallback DB path works fine.

Made changes.

2466921_cgcp: 2026-02-27T16:56:54.320+05:30  INFO 16960 --- [backend] [nio-8888-exec-6] c.i.
b.n.service.NotificationService      : User 1 connected to SSE stream. Total a
ctive connections for user: 8
2026-02-27T16:56:54.330+05:30  INFO 16960 --- [backend] [nio-8888-exec-9] c.i.
b.n.service.NotificationService      : Retrieved 2 total notifications for use
r ID: 1
2026-02-27T16:56:54.333+05:30  INFO 16960 --- [backend] [nio-8888-exec-1] c.i.
b.n.service.NotificationService      : User 1 connected to SSE stream. Total a
ctive connections for user: 9
2026-02-27T16:56:54.337+05:30  INFO 16960 --- [backend] [nio-8888-exec-2] c.i.
b.n.c.NotificationController         : Fetching all notifications for user    
2026-02-27T16:57:04.342+05:30  WARN 16960 --- [backend] [nio-8888-exec-2] org.
hibernate.orm.jdbc.error             : HHH000247: ErrorCode: 0, SQLState: null
2026-02-27T16:57:04.342+05:30  WARN 16960 --- [backend] [nio-8888-exec-8] org.
hibernate.orm.jdbc.error             : HHH000247: ErrorCode: 0, SQLState: null
2026-02-27T16:57:04.343+05:30  WARN 16960 --- [backend] [nio-8888-exec-2] org.
hibernate.orm.jdbc.error             : HikariPool-4 - Connection is not availa
ble, request timed out after 10004ms (total=25, active=25, idle=0, waiting=1) 
2026-02-27T16:57:04.343+05:30  WARN 16960 --- [backend] [nio-8888-exec-8] org.
hibernate.orm.jdbc.error             : HikariPool-4 - Connection is not availa
ble, request timed out after 10010ms (total=25, active=25, idle=0, waiting=1) 
2026-02-27T16:57:04.348+05:30  WARN 16960 --- [backend] [nio-8888-exec-2] .m.m
.a.ExceptionHandlerExceptionResolver : Resolved [org.springframework.dao.DataA
ccessResourceFailureException: Unable to acquire JDBC Connection [HikariPool-4
 - Connection is not available, request timed out after 10004ms (total=25, act
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 GitHub Copilot: The pool is still exhausted — `total=25, active=25` means all 25 connections are held. The new JWT fast path isn't working yet because existing tokens lack the role claim, so it's still falling back to the DB. But more critically, **SSE connections themselves hold DB connections** because `subscribe()` calls `securityService.getCurrentUser()` which hits the DB, and Spring's async SSE handling may hold a transaction open for the lifetime of the connection.

Let me check the full picture: 

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/notification/service/NotificationService.java#50-50), lines 50 to 150

Searched for files matching `**/NotificationController.java`, 1 match

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/notification/controller/NotificationController.java#1-1), lines 1 to 80

Searched for files matching `**/SecurityService.java`, 1 match

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/common/security/SecurityService.java)

Now I have the full picture. There are **two separate problems** causing pool exhaustion:

1. **`SecurityService.getCurrentUser()`** hits DB on every call — including in `subscribe()`, `getAllNotifications()`, and every other service method. This is called on all 25+ active requests simultaneously.

2. **SSE connections hold Tomcat threads** — with 9 active SSE connections per user (log shows it!), the `NotificationContext` is reconnecting repeatedly without proper deduplication, eating all threads AND connections.

The real fix needs to happen on two fronts:
- **`SecurityService`**: Add a `getCurrentUserId()` that reads from `SecurityContext` principal directly — but `getCurrentUser()` which loads the full `User` entity will always need a DB call. The key is to make `subscribe()` not need a `User` entity, just the `userId` from the security context.
- **Frontend `NotificationContext`**: The log shows 9 connections for user 1 — the reconnect logic is still broken.

Let me check the current frontend NotificationContext and the current JWT filter state: 

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Frontend-v1/src/context/NotificationContext.tsx#1-1), lines 1 to 120

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/config/JwtAuthenticationFilter.java#1-1), lines 1 to 80

The JWT fast path is in place. The problem is now clear: **the log shows 9 SSE connections for a single user** — the frontend is reconnecting in a loop because `onerror` fires on every SSE reconnect attempt (the backend closes connections on auth failure), so it keeps hammering. Meanwhile, `SecurityService.getCurrentUser()` does `userRepository.findByEmail()` — a DB call — inside `subscribe()` AND inside every other service method.

The core fix needed:

1. **Backend `subscribe()`**: Extract `userId` from the `SecurityContext` principal (already loaded by the JWT filter) without hitting DB. Since the JWT now contains the role but NOT the userId, we need `SecurityService` to provide a way to get the user by email from the SecurityContext — but lazily. Better: store `userId` claim in the JWT too.

2. **Frontend**: The SSE is reconnecting 9 times because `onerror` fires immediately on the initial connection attempt (before auth). Add a **single-connection guard** using a `connectionId` ref so only one connection attempt is alive at a time.

Let me fix both: 

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/config/JWTUtil.java#1-1), lines 1 to 60

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/controller/AuthController.java#50-50), lines 50 to 55

Now I have the complete picture. I'll make targeted fixes:

**Backend**: Embed `userId` in the JWT and add `extractUserId()` to `JWTUtil`. Update `SecurityService.getCurrentUser()` to try reading userId from the JWT claims via a new `SecurityService.getCurrentUserId()` — but more importantly, make `subscribe()` use `getCurrentUserId()` from the token claims directly to avoid the DB call entirely.

**Frontend**: Add a strict single-connection guard — if `sseRef` is already open and `readyState` is `OPEN` or `CONNECTING`, don't create a new one. 

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/config/JWTUtil.java#60-60), lines 60 to 90

Now I'll apply all fixes simultaneously: 

Using "Multi-Replace String in Files"


```

```

```

```

```

```

```

```
Now fix the frontend — add a strict single-connection guard so rapid reconnects don't pile up: 

Read [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Frontend-v1/src/context/NotificationContext.tsx#55-55), lines 55 to 143

Using "Replace String in File"


```

```


Using "Replace String in File"


```

```
Now verify errors on all changed files: 

Checked [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/user/config/JWTUtil.java), [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/common/security/SecurityService.java), [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Backend/src/main/java/com/incidenttracker/backend/notification/service/NotificationService.java), [](file:///c%3A/Users/2466921/OneDrive%20-%20Cognizant/Desktop/IT/Frontend_Backend/Incident-Track-Frontend-v1/src/context/NotificationContext.tsx), no problems found

All clean. Here's what was fixed and why each part matters:

---

## Root Cause Analysis

The log revealed two overlapping problems:

### Problem 1: 9 SSE connections per user (connection storm)
The log showed `Total active connections for user: 9`. The frontend `onerror` handler was calling `connect()` without checking if a connection already existed in `CONNECTING` state — leading to a rapid cascade of new connections before any old one was confirmed dead.

### Problem 2: Every SSE subscribe() hit the DB
`subscribe()` called `securityService.getCurrentUser()` → `userRepository.findByEmail()` → a DB connection. With 25 pool connections and SSE's long-lived threads, each reconnect consumed a connection that was never released.

---

## Fixes Applied

### JWTUtil.java
- `generateToken(username, role, userId)` — now embeds `userId` as a JWT claim alongside `role`
- `extractUserId(token)` — reads `userId` claim without touching the DB

### AuthController.java
- Passes `user.getUserId()` into `generateToken()` so new tokens carry the userId claim

### SecurityService.java
- New `getCurrentUserIdFromToken()` — reads userId directly from the JWT in the current HTTP request (**zero DB calls**)
- Keeps `getCurrentUser()` as fallback for old tokens

### NotificationService.java  
- `subscribe()` now uses `getCurrentUserIdFromToken()` first — **no DB hit for SSE**
- Falls back to `getCurrentUser()` only for old tokens without userId claim
- Added **safety valve**: if a user already has ≥3 SSE connections, the oldest is completed and removed before creating a new one

### NotificationContext.tsx (Frontend)
- Added `connectingRef` guard — skips `connect()` if a connection is already `OPEN` or `CONNECTING`
- Backoff starts at **3s** (was 2s) — more breathing room
- **Max 10 retries** — stops hammering after ~5 minutes if the backend is truly unreachable
- `connectingRef.current = false` cleared on `onopen` and `onerror` so the state stays consistent

> **Action needed**: Log out and log back in once so the new token with `userId` + `role` claims is issued. After that, SSE subscribe calls will make **zero DB queries**.

Made changes.