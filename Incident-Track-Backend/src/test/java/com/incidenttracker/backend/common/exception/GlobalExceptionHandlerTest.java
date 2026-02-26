package com.incidenttracker.backend.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    // Runs before each test to prepare common setup.
    @BeforeEach
    void setUp() {
        // Build a lightweight MockMvc with our test controller + the global exception handler.
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Marks a method as a test case.
    @Test
    void handleResourceNotFound_returnsNotFoundAndMap() throws Exception {
        // Verifies ResourceNotFoundException is mapped to 404 with the expected JSON shape.
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("missing"));
    }

    @Test
    void handleIllegalState_returnsConflictAndMap() throws Exception {
        // Verifies IllegalStateException is mapped to 409 with the expected JSON shape.
        mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("invalid state"));
    }

    @Test
    void handleBadRequest_returnsBadRequestAndMap() throws Exception {
        // Verifies BadRequestException is mapped to 400 with the expected JSON shape.
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("bad request"));
    }

    @Test
    void handleException_returnsInternalServerErrorAndMap() throws Exception {
        // Verifies generic Exception is mapped to 500 with the expected JSON shape.
        mockMvc.perform(get("/test/exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred: boom"));
    }

    // Simple controller used only for test endpoints.
    @RestController
    static class TestController {
        // Minimal endpoints that throw exceptions to exercise the handler.
        @GetMapping("/test/not-found")
        public String notFound() {
            throw new ResourceNotFoundException("missing");
        }

        @GetMapping("/test/illegal-state")
        public String illegalState() {
            throw new IllegalStateException("invalid state");
        }

        @GetMapping("/test/runtime")
        public String runtime() {
            throw new RuntimeException("runtime error");
        }

        @GetMapping("/test/bad-request")
        public String badRequest() {
            throw new BadRequestException("bad request");
        }

        @GetMapping("/test/exception")
        public String exception() throws Exception {
            throw new Exception("boom");
        }
    }
}
