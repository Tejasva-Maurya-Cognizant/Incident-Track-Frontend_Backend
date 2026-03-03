# Auth And Users

This module covers login, JWT handling, user profile access, admin user management, and the shared helper used by other modules to identify the current user.

## Core files

- [AuthController.java](../../../src/main/java/com/incidenttracker/backend/user/controller/AuthController.java)
- [UserService.java](../../../src/main/java/com/incidenttracker/backend/user/service/UserService.java)
- [AdminService.java](../../../src/main/java/com/incidenttracker/backend/user/service/AdminService.java)
- [CustomUserDetailsService.java](../../../src/main/java/com/incidenttracker/backend/user/service/CustomUserDetailsService.java)
- [UserRepository.java](../../../src/main/java/com/incidenttracker/backend/user/repository/UserRepository.java)
- [User.java](../../../src/main/java/com/incidenttracker/backend/user/entity/User.java)
- [SecurityConfig.java](../../../src/main/java/com/incidenttracker/backend/user/config/SecurityConfig.java)
- [JWTUtil.java](../../../src/main/java/com/incidenttracker/backend/user/config/JWTUtil.java)
- [JwtAuthenticationFilter.java](../../../src/main/java/com/incidenttracker/backend/user/config/JwtAuthenticationFilter.java)
- [SecurityService.java](../../../src/main/java/com/incidenttracker/backend/common/security/SecurityService.java)

## What each file does

- `AuthController.java`
  Owns `/api/auth` endpoints. It handles login, registration, profile lookup, user listing, manager department lookups, self-update, and admin-only user edits.
- `UserService.java`
  Maps `User` entities into `UserResponseDto`, implements paged/non-paged user queries, and supports self-service username/password updates.
- `AdminService.java`
  Contains admin-only behaviors such as toggling user status and editing another user's role, department, email, or password.
- `UserRepository.java`
  Central repository for email lookup, username lookup, role filters, and department-scoped employee queries. This repository is reused by security, notification, and task logic.
- `SecurityConfig.java`
  Configures CORS, disables server-side sessions, allows a small set of public endpoints, and injects the JWT filter before standard username/password handling.
- `SecurityService.java`
  This is the shared "who is the current user?" helper. Other modules depend on it instead of duplicating Spring Security context parsing.

## Auth flow

1. Login hits `POST /api/auth/login`.
2. `AuthenticationManager` validates email/password.
3. The controller loads the matching user record from `UserRepository`.
4. Inactive users are blocked even if their credentials are correct.
5. `JWTUtil` generates a token that includes email, role, and user id.
6. The frontend stores the token and sends it back in the `Authorization` header.

## Role and access model

- `ADMIN`
  Full user management and full privileged access elsewhere in the backend.
- `MANAGER`
  Can view users and see employees in the manager's own department.
- `EMPLOYEE`
  Can only access self-service profile and self-update endpoints in this module.

The important pattern is that controllers enforce broad role checks with `@PreAuthorize`, while services enforce more specific workflow constraints.

## Useful repository methods

`UserRepository.java` is one of the best files to skim because its derived query names expose the data model clearly:

- `findByEmail(...)`
- `findByUsername(...)`
- `findByRole(...)`
- `findByDepartment_DepartmentId(...)`
- `findByRoleAndDepartment_DepartmentId(...)`

Those same methods are reused by notifications, task assignment validation, and profile lookups.

## Practical notes

- Registration requires a valid `departmentId`.
- Passwords are encoded with BCrypt before persistence.
- `SecurityService.getCurrentUserIdFromToken()` is a useful fast path when code only needs the id and wants to avoid an extra database lookup.
