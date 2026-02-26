package com.incidenttracker.backend.department.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.incidenttracker.backend.department.dto.DepartmentRequestDto;
import com.incidenttracker.backend.department.dto.DepartmentResponseDto;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.department.repository.DepartmentRepository;
import com.incidenttracker.backend.department.service.impl.DepartmentServiceImpl;
import com.incidenttracker.backend.common.exception.ConflictException;

// Enables Mockito support in JUnit 5 so mocks are created and injected automatically.
@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    // @Mock: replaces repository dependency with a Mockito stub.
    @Mock
    private DepartmentRepository departmentRepository;

    // @InjectMocks: creates service instance with mocked dependencies.
    @InjectMocks
    private DepartmentServiceImpl departmentService;

    // @Test: marks method as an executable unit test.
    @Test
    // Scenario: create trims name, saves entity, and returns mapped DTO.
    void createDepartment_returnsResponse() {
        DepartmentRequestDto request = new DepartmentRequestDto();
        request.setDepartmentName("  IT  ");

        when(departmentRepository.findByDepartmentName("IT")).thenReturn(Optional.empty());

        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department dept = invocation.getArgument(0);
            dept.setDepartmentId(1L);
            return dept;
        });

        DepartmentResponseDto response = departmentService.createDepatment(request);

        assertNotNull(response);
        assertEquals(1L, response.getDepartmentId());
        assertEquals("IT", response.getDepartmentName());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    // Scenario: duplicate department name should throw ConflictException.
    void createDepartment_whenDuplicate_throwsConflict() {
        DepartmentRequestDto request = new DepartmentRequestDto();
        request.setDepartmentName("IT");

        when(departmentRepository.findByDepartmentName("IT"))
                .thenReturn(Optional.of(Department.builder().departmentId(1L).departmentName("IT").build()));

        assertThrows(ConflictException.class, () -> departmentService.createDepatment(request));
    }

    @Test
    // Scenario: getAll maps each Department entity to DepartmentResponseDto.
    void getAllDepartments_mapsEntities() {
        Department d1 = Department.builder().departmentId(1L).departmentName("IT").build();
        Department d2 = Department.builder().departmentId(2L).departmentName("HR").build();

        when(departmentRepository.findAll()).thenReturn(List.of(d1, d2));

        List<DepartmentResponseDto> result = departmentService.getAllDepartments();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getDepartmentId());
        assertEquals("IT", result.get(0).getDepartmentName());
        assertEquals(2L, result.get(1).getDepartmentId());
        assertEquals("HR", result.get(1).getDepartmentName());
        verify(departmentRepository).findAll();
    }
}
