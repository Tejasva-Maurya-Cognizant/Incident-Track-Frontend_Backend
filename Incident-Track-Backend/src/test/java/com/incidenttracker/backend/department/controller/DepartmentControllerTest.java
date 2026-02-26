package com.incidenttracker.backend.department.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidenttracker.backend.department.dto.DepartmentRequestDto;
import com.incidenttracker.backend.department.dto.DepartmentResponseDto;
import com.incidenttracker.backend.department.service.DepartmentService;

// Enables Mockito support in JUnit 5 so @Mock and @InjectMocks are initialized automatically.
@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

        private MockMvc mockMvc;

        private ObjectMapper objectMapper;

        // @Mock: creates a test double for the service dependency.
        @Mock
        private DepartmentService departmentService;

        // @InjectMocks: builds controller and injects mocked collaborators.
        @InjectMocks
        private DepartmentController departmentController;

        // @BeforeEach: runs before every test method.
        @BeforeEach
        // Setup: create shared objects so each test runs in a predictable state.
        void setUp() {
                objectMapper = new ObjectMapper();
                mockMvc = MockMvcBuilders.standaloneSetup(departmentController).build();
        }

        // @Test: marks this method as an executable test case.
        @Test
        // Scenario: POST create department returns 201 and response body with saved
        // fields.
        void createDepartment_returnsCreatedAndBody() throws Exception {
                DepartmentRequestDto request = new DepartmentRequestDto();
                request.setDepartmentName("IT");

                DepartmentResponseDto response = DepartmentResponseDto.builder()
                                .departmentId(1L)
                                .departmentName("IT")
                                .build();

                when(departmentService.createDepatment(any(DepartmentRequestDto.class))).thenReturn(response);

                mockMvc.perform(post("/api/departments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.departmentId").value(1L))
                                .andExpect(jsonPath("$.departmentName").value("IT"));

                ArgumentCaptor<DepartmentRequestDto> captor = ArgumentCaptor.forClass(DepartmentRequestDto.class);
                verify(departmentService).createDepatment(captor.capture());
                org.junit.jupiter.api.Assertions.assertEquals("IT", captor.getValue().getDepartmentName());
        }

        @Test
        // Scenario: GET all departments returns 200 and a list with expected
        // size/content.
        void getAllDepartments_returnsOkAndList() throws Exception {
                DepartmentResponseDto d1 = DepartmentResponseDto.builder()
                                .departmentId(1L)
                                .departmentName("IT")
                                .build();
                DepartmentResponseDto d2 = DepartmentResponseDto.builder()
                                .departmentId(2L)
                                .departmentName("HR")
                                .build();

                when(departmentService.getAllDepartments()).thenReturn(List.of(d1, d2));

                mockMvc.perform(get("/api/departments"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[0].departmentId").value(1L))
                                .andExpect(jsonPath("$[1].departmentId").value(2L));

                verify(departmentService).getAllDepartments();
        }
}
