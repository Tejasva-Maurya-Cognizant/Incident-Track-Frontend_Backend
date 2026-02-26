package com.incidenttracker.backend.department.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.incidenttracker.backend.department.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Used to enforce unique department names during create flow.
    public Optional<Department> findByDepartmentName(String departmentName);

    // ---- Pageable version ----
    Page<Department> findAll(Pageable pageable);
}
