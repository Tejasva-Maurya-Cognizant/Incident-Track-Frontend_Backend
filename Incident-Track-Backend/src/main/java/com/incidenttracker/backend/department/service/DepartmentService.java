package com.incidenttracker.backend.department.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.department.dto.DepartmentRequestDto;
import com.incidenttracker.backend.department.dto.DepartmentResponseDto;

public interface DepartmentService {

    // Validates and creates a new department, returning the saved data.
    DepartmentResponseDto createDepatment(DepartmentRequestDto request);

    // Returns all departments for list/dropdown style UI usage.
    List<DepartmentResponseDto> getAllDepartments();

    // Returns one department by id or throws not-found when missing.
    DepartmentResponseDto getDepartmentById(Long departmentId);

    // Returns paginated, sorted departments.
    PagedResponse<DepartmentResponseDto> getAllDepartmentsPaged(Pageable pageable);
}
