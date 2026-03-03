# Reference Data

This module covers the supporting master data used by the rest of the system: departments and categories.

## Core files

- [DepartmentController.java](../../../src/main/java/com/incidenttracker/backend/department/controller/DepartmentController.java)
- [DepartmentServiceImpl.java](../../../src/main/java/com/incidenttracker/backend/department/service/impl/DepartmentServiceImpl.java)
- [DepartmentRepository.java](../../../src/main/java/com/incidenttracker/backend/department/repository/DepartmentRepository.java)
- [Department.java](../../../src/main/java/com/incidenttracker/backend/department/entity/Department.java)
- [CategoryController.java](../../../src/main/java/com/incidenttracker/backend/category/controller/CategoryController.java)
- [CategoryService.java](../../../src/main/java/com/incidenttracker/backend/category/service/CategoryService.java)
- [CategoryRepository.java](../../../src/main/java/com/incidenttracker/backend/category/repository/CategoryRepository.java)
- [Category.java](../../../src/main/java/com/incidenttracker/backend/category/entity/Category.java)

## Department module

The department module is intentionally small:

- list all departments
- page through departments
- fetch one department by id
- create a new department

`DepartmentServiceImpl.java` is where the meaningful validation lives:

- blank names are rejected
- department names are normalized with `trim()`
- duplicate department names are blocked before insert

Departments are important because they drive:

- user-to-department assignments
- manager scope
- category ownership
- incident and task visibility
- reporting groupings

## Category module

Categories define the operational metadata for incidents:

- parent category name
- sub-category name
- SLA hours
- owning department
- visibility flag

`CategoryService.java` is the key file because it:

- maps entities to the API response DTO
- creates categories with a required department reference
- updates category fields selectively
- supports soft-hide behavior through `toggleVisibility(...)`
- returns paged full lists and paged visible-only lists

## How categories affect incident behavior

The category module is not just lookup data. It directly affects runtime behavior because `IncidentServiceImpl.java` uses category values to:

- compute incident severity
- compute the effective SLA deadline
- attach the incident to a department through the category's department

That means changing category records changes future incident behavior.

## Practical notes

- Categories are visible to all roles for selection and display.
- Only admins can create, update, or toggle category visibility.
- Department lookup is public in `SecurityConfig`, which allows the frontend to populate registration and setup forms before login.
