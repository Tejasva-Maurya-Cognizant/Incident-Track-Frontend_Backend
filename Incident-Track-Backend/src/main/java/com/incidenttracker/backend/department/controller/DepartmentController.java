package com.incidenttracker.backend.department.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.department.dto.DepartmentRequestDto;
import com.incidenttracker.backend.department.dto.DepartmentResponseDto;
import com.incidenttracker.backend.department.service.DepartmentService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // Returns all departments (non-paged).
    @GetMapping()
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    /**
     * GET /api/departments/paged?page=0&size=10&sortBy=departmentName&sortDir=asc
     */
    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<DepartmentResponseDto>> getAllDepartmentsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "departmentName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(departmentService.getAllDepartmentsPaged(pageable));
    }

    // Fetches one department by its id.
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseDto> getDepartmentById(@PathVariable("departmentId") Long departmentId) {
        return ResponseEntity.ok(departmentService.getDepartmentById(departmentId));
    }

    // Creates a new department.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public ResponseEntity<DepartmentResponseDto> createDepartment(@Valid @RequestBody DepartmentRequestDto request) {
        return new ResponseEntity<>(departmentService.createDepatment(request), HttpStatus.CREATED);
    }
}
