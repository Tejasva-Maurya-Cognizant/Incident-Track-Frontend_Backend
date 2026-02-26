package com.incidenttracker.backend.department.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.common.exception.BadRequestException;
import com.incidenttracker.backend.common.exception.ConflictException;
import com.incidenttracker.backend.common.exception.ResourceNotFoundException;
import com.incidenttracker.backend.department.dto.DepartmentRequestDto;
import com.incidenttracker.backend.department.dto.DepartmentResponseDto;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.department.repository.DepartmentRepository;
import com.incidenttracker.backend.department.service.DepartmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    // Validates department name, prevents duplicates, and persists a new row.
    // Uses trimmed name so stored values stay consistent.
    public DepartmentResponseDto createDepatment(DepartmentRequestDto request) {

        String name = request.getDepartmentName();
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Department name is required");
        }

        String normalized = name.trim();
        if (departmentRepository.findByDepartmentName(normalized).isPresent()) {
            throw new ConflictException("Department already exists: " + normalized);
        }

        Department department = Department.builder().departmentName(normalized).build();

        Department saved = departmentRepository.save(department);

        return DepartmentResponseDto.builder().departmentName(saved.getDepartmentName())
                .departmentId(saved.getDepartmentId()).build();

    }

    @Override
    // Fetches all departments and maps entities to lightweight response DTOs.
    public List<DepartmentResponseDto> getAllDepartments() {

        return departmentRepository.findAll().stream().map(dept -> DepartmentResponseDto.builder()
                .departmentName(dept.getDepartmentName()).departmentId(dept.getDepartmentId()).build()).toList();

    }

    @Override
    // Loads a single department by id and surfaces a clear not-found error.
    public DepartmentResponseDto getDepartmentById(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        return DepartmentResponseDto.builder().departmentName(department.getDepartmentName())
                .departmentId(department.getDepartmentId()).build();
    }

    @Override
    // Returns a paginated, sorted slice of departments.
    public PagedResponse<DepartmentResponseDto> getAllDepartmentsPaged(Pageable pageable) {
        Page<Department> page = departmentRepository.findAll(pageable);
        return PagedResponse.<DepartmentResponseDto>builder()
                .content(page.getContent().stream()
                        .map(dept -> DepartmentResponseDto.builder()
                                .departmentName(dept.getDepartmentName())
                                .departmentId(dept.getDepartmentId())
                                .build())
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
