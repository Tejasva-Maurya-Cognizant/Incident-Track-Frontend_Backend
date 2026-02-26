package com.incidenttracker.backend.incident.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.common.exception.GlobalExceptionHandler;
import com.incidenttracker.backend.common.exception.ResourceNotFoundException;
import com.incidenttracker.backend.incident.dto.IncidentRequestDTO;
import com.incidenttracker.backend.incident.dto.IncidentResponseDTO;
import com.incidenttracker.backend.incident.dto.IncidentStatusUpdateRequestDTO;
import com.incidenttracker.backend.incident.service.IncidentService;
import com.incidenttracker.backend.user.config.JWTUtil;
import com.incidenttracker.backend.user.service.CustomUserDetailsService;

/**
 * 
 * Unit Tests for IncidentController.
 * 
 * * Uses @WebMvcTest to slice only the Controller layer.
 * 
 * Security filters are disabled (addFilters = false) to focus on logic.
 * 
 * GlobalExceptionHandler is explicitly imported to test error responses.
 * 
 */

@WebMvcTest(controllers = IncidentController.class)

@AutoConfigureMockMvc(addFilters = false) // Bypass Security (JWT/Login)

@Import(GlobalExceptionHandler.class) // Load custom Exception Handler

class IncidentControllerTest {

    // Injects a Spring-managed bean into the test.
    @Autowired

    private MockMvc mockMvc; // Simulates HTTP requests

    // Creates a Mockito mock for isolating dependencies.
    @MockitoBean

    private IncidentService incidentService; // Mocks the Service layer

    @MockitoBean

    private JWTUtil jwtUtil;

    @MockitoBean

    private CustomUserDetailsService customUserDetailsService;

    @Autowired

    private ObjectMapper objectMapper; // Converts Java Objects <-> JSON

    private IncidentResponseDTO mockResponseDTO;

    // Runs before each test to prepare common setup.
    @BeforeEach

    void setUp() {

        // Prepare a standard response object to be used in multiple tests

        mockResponseDTO = new IncidentResponseDTO(

                1L,

                101L,

                "System crash",

                55L,

                "john_doe",

                IncidentStatus.OPEN,

                IncidentSeverity.HIGH,

                false,

                LocalDateTime.now(),

                "Hardware",

                4

        );

    }

    // Marks a method as a test case.
    @TestConfiguration

    static class TestConfig {

        @Bean

        public ObjectMapper objectMapper() {

            ObjectMapper mapper = new ObjectMapper();

            // This module is required to handle LocalDateTime correctly in JSON

            mapper.registerModule(new JavaTimeModule());

            return mapper;

        }

    }

    // ==========================================

    // POSITIVE TESTS (Happy Path)

    // ==========================================

    /**
     * 
     * Test Case 1: Create a new incident.
     * 
     * Scenario: User sends a valid POST request with incident details.
     * 
     * Mock Behavior: Service returns the saved incident DTO.
     * 
     * Expected: HTTP 201 Created, JSON body contains the created incident ID and
     * description.
     * 
     */

    @Test

    // Provides a readable name for the test in reports.
    @DisplayName("POST /api/incidents - Create Incident Success")

    void createIncident_Success() throws Exception {

        IncidentRequestDTO requestDTO = new IncidentRequestDTO(101L, "System crash", false);

        when(incidentService.createIncident(any(IncidentRequestDTO.class)))

                .thenReturn(mockResponseDTO);

        mockMvc.perform(post("/api/incidents")

                .contentType(MediaType.APPLICATION_JSON)

                .content(objectMapper.writeValueAsString(requestDTO)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.incidentId").value(1L))

                .andExpect(jsonPath("$.description").value("System crash"));

    }

    /**
     * 
     * Test Case 2: Get all incidents for the logged-in user.
     * 
     * Scenario: User requests their own incidents.
     * 
     * Mock Behavior: Service returns a list containing one incident.
     * 
     * Expected: HTTP 200 OK, JSON array size is 1.
     * 
     */

    @Test

    @DisplayName("GET /api/incidents - Get User Incidents Success")

    void getIncidentsByUser_Success() throws Exception {

        List<IncidentResponseDTO> list = Arrays.asList(mockResponseDTO);

        when(incidentService.getIncidentsUser()).thenReturn(list);

        mockMvc.perform(get("/api/incidents"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.size()").value(1))

                .andExpect(jsonPath("$[0].incidentId").value(1L));

    }

    /**
     * 
     * Test Case 3: Get incidents filtered by Status.
     * 
     * Scenario: User requests all 'OPEN' incidents.
     * 
     * Mock Behavior: Service returns a list of OPEN incidents.
     * 
     * Expected: HTTP 200 OK, JSON status field is 'OPEN'.
     * 
     */

    @Test

    @DisplayName("GET /api/incidents/status/{status} - Get by Status Success")

    void getIncidentsByStatus_Success() throws Exception {

        List<IncidentResponseDTO> list = Arrays.asList(mockResponseDTO);

        when(incidentService.getIncidentsByUserAndStatus(IncidentStatus.OPEN))

                .thenReturn(list);

        mockMvc.perform(get("/api/incidents/status/OPEN"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$[0].status").value("OPEN"));

    }

    /**
     * 
     * Test Case 4: Get incidents filtered by Calculated Severity.
     * 
     * Scenario: User requests all 'HIGH' severity incidents.
     * 
     * Mock Behavior: Service returns a list of HIGH severity incidents.
     * 
     * Expected: HTTP 200 OK, JSON severity field is 'HIGH'.
     * 
     */

    @Test

    @DisplayName("GET /api/incidents/calculatedSeverity/{severity} - Get by Severity Success")

    void getIncidentsBySeverity_Success() throws Exception {

        List<IncidentResponseDTO> list = Arrays.asList(mockResponseDTO);

        when(incidentService.getIncidentsByUserAndCalculatedSeverity(IncidentSeverity.HIGH))

                .thenReturn(list);

        mockMvc.perform(get("/api/incidents/calculatedSeverity/HIGH"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$[0].calculatedSeverity").value("HIGH"));

    }

    /**
     * 
     * Test Case 5: Get critical incidents shortcut.
     * 
     * Scenario: User calls the specific endpoint for CRITICAL incidents.
     * 
     * Mock Behavior: Service returns a list where severity is CRITICAL.
     * 
     * Expected: HTTP 200 OK, JSON severity field is 'CRITICAL'.
     * 
     */

    @Test

    @DisplayName("GET /api/incidents/CRITICAL - Get Critical Incidents Success")

    void getCriticalIncidents_Success() throws Exception {

        mockResponseDTO.setIsCritical(true);

        List<IncidentResponseDTO> list = Arrays.asList(mockResponseDTO);

        when(incidentService.getIncidentsByUserAndUserMarkedCritical(Boolean.TRUE))

                .thenReturn(list);

        mockMvc.perform(get("/api/incidents/CRITICAL"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$[0].isCritical").value(true));

    }

    /**
     * 
     * Test Case 6: Get details of a specific incident.
     * 
     * Scenario: User requests details for ID 1.
     * 
     * Mock Behavior: Service returns the specific incident DTO.
     * 
     * Expected: HTTP 200 OK, JSON ID matches 1.
     * 
     */

    @Test

    @DisplayName("GET /api/incidents/{incidentId} - Get Details Success")

    void getIncidentDetails_Success() throws Exception {

        when(incidentService.getIncidentDetails(1L)).thenReturn(mockResponseDTO);

        mockMvc.perform(get("/api/incidents/{incidentId}", 1L))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.incidentId").value(1L));

    }

    /**
     * 
     * Test Case 7: Withdraw (Cancel) an incident.
     * 
     * Scenario: User cancels incident 1.
     * 
     * Mock Behavior: Service updates status to CANCELLED and returns updated DTO.
     * 
     * Expected: HTTP 200 OK, JSON status is 'CANCELLED'.
     * 
     */

    @Test

    @DisplayName("PUT /api/incidents/withdraw/{incidentId} - Withdraw Success")

    void withdrawIncident_Success() throws Exception {

        mockResponseDTO.setStatus(IncidentStatus.CANCELLED);

        when(incidentService.withdrawIncident(1L)).thenReturn(mockResponseDTO);

        mockMvc.perform(put("/api/incidents/withdraw/{incidentId}", 1L))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.status").value("CANCELLED"));

    }

    /**
     * 
     * Test Case 8: Admin fetches all incidents.
     * 
     * Scenario: Admin requests the full list of incidents.
     * 
     * Mock Behavior: Service returns a list of 2 incidents.
     * 
     * Expected: HTTP 200 OK, JSON array size is 2.
     * 
     */

    @Test

    @DisplayName("GET /api/incidents/admin - Get All Admin Incidents Success")

    void getAllIncidentsForAdmin_Success() throws Exception {

        List<IncidentResponseDTO> list = Arrays.asList(mockResponseDTO, mockResponseDTO);

        when(incidentService.getAllIncidents()).thenReturn(list);

        mockMvc.perform(get("/api/incidents/admin"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.size()").value(2));

    }

    /**
     * 
     * Test Case 9: Update incident status.
     * 
     * Scenario: User/Admin updates status to RESOLVED with a note.
     * 
     * Mock Behavior: Service updates and returns the DTO with status RESOLVED.
     * 
     * Expected: HTTP 200 OK, JSON status is 'RESOLVED'.
     * 
     */

    @Test

    @DisplayName("PUT /api/incidents/{incidentId}/status - Update Status Success")

    void updateStatus_Success() throws Exception {

        IncidentStatusUpdateRequestDTO request =

                new IncidentStatusUpdateRequestDTO(IncidentStatus.RESOLVED, "Done");

        mockResponseDTO.setStatus(IncidentStatus.RESOLVED);

        when(incidentService.updateIncidentStatus(eq(1L), any(IncidentStatusUpdateRequestDTO.class)))

                .thenReturn(mockResponseDTO);

        mockMvc.perform(put("/api/incidents/{incidentId}/status", 1L)

                .contentType(MediaType.APPLICATION_JSON)

                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.status").value("RESOLVED"));

    }

    // ==========================================

    // NEGATIVE TESTS (Error Handling)

    // ==========================================

    /**
     * 
     * Negative Test 1: Resource Not Found.
     * 
     * Scenario: User requests an incident ID that does not exist.
     * 
     * Mock Behavior: Service throws ResourceNotFoundException.
     * 
     * Expected: HTTP 404 Not Found.
     * 
     * JSON Check: Checks 'error' field (as per GlobalExceptionHandler returning
     * Map).
     * 
     */

    @Test

    @DisplayName("Negative: ResourceNotFoundException (Returns Map -> 'error')")

    void getIncidentDetails_NotFound() throws Exception {

        when(incidentService.getIncidentDetails(999L))

                .thenThrow(new ResourceNotFoundException("Incident not found with id 999"));

        mockMvc.perform(get("/api/incidents/{incidentId}", 999L))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.message").value("Incident not found with id 999"));

    }

    /**
     * 
     * Negative Test 2: Workflow Conflict.
     * 
     * Scenario: User tries to perform an illegal action (e.g., transition state
     * improperly).
     * 
     * Mock Behavior: Service throws IllegalStateException.
     * 
     * Expected: HTTP 409 Conflict.
     * 
     * JSON Check: Checks 'error' field (as per GlobalExceptionHandler returning
     * Map).
     * 
     */

    @Test

    @DisplayName("Negative: IllegalStateException (Returns Map -> 'error')")

    void createIncident_Conflict() throws Exception {

        IncidentRequestDTO requestDTO = new IncidentRequestDTO(1L, "Conflict Test", false);

        when(incidentService.createIncident(any(IncidentRequestDTO.class)))

                .thenThrow(new IllegalStateException("Workflow violation"));

        mockMvc.perform(post("/api/incidents")

                .contentType(MediaType.APPLICATION_JSON)

                .content(objectMapper.writeValueAsString(requestDTO)))

                .andExpect(status().isConflict())

                .andExpect(jsonPath("$.message").value("Workflow violation"));

    }

    /**
     * 
     * Negative Test 3: Generic Runtime Exception.
     * 
     * Scenario: An unexpected runtime error occurs in the service.
     * 
     * Mock Behavior: Service throws RuntimeException.
     * 
     * Expected: HTTP 404 Not Found (As per GlobalExceptionHandler logic for
     * RuntimeException).
     * 
     * JSON Check: Checks 'message' field (as per GlobalExceptionHandler returning
     * ErrorResponse object).
     * 
     */

    @Test

    @DisplayName("Negative: Generic RuntimeException (Returns ErrorResponse -> 'message')")

    void getIncidentDetails_GenericError() throws Exception {

        when(incidentService.getIncidentDetails(500L))

                .thenThrow(new RuntimeException("Something went wrong internally"));

        mockMvc.perform(get("/api/incidents/{incidentId}", 500L))

                .andExpect(status().isInternalServerError())

                .andExpect(jsonPath("$.message").value("An unexpected error occurred: Something went wrong internally"));

    }

    /**
     * 
     * Negative Test 4: Uncaught Global Exception.
     * 
     * Scenario: A severe system error occurs (e.g. checked Exception).
     * 
     * Mock Behavior: Service throws a generic Exception.
     * 
     * Expected: HTTP 500 Internal Server Error.
     * 
     * JSON Check: Checks 'message' field containing "An unexpected error occurred".
     * 
     */

    @Test

    @DisplayName("Negative: Uncaught Exception (Returns 500)")

    void createIncident_ServerError() throws Exception {

        IncidentRequestDTO requestDTO = new IncidentRequestDTO(1L, "Crash Test", false);

        // Note: createIncident returns an object, so we use when(...).thenAnswer(...)
        // to throw Exception

        when(incidentService.createIncident(any(IncidentRequestDTO.class)))

                .thenAnswer(invocation -> {
                    throw new Exception("Database Down");
                });

        mockMvc.perform(post("/api/incidents")

                .contentType(MediaType.APPLICATION_JSON)

                .content(objectMapper.writeValueAsString(requestDTO)))

                .andExpect(status().isInternalServerError())

                .andExpect(jsonPath("$.message").value("An unexpected error occurred: Database Down"));

    }

}
