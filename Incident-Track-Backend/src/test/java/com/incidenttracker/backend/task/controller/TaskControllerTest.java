package com.incidenttracker.backend.task.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.incidenttracker.backend.common.enums.TaskStatus;
import com.incidenttracker.backend.task.dto.TaskRequestDto;
import com.incidenttracker.backend.task.dto.TaskResponseDto;
import com.incidenttracker.backend.task.dto.TaskStatusUpdateRequestDto;
import com.incidenttracker.backend.task.service.impl.TaskServiceImpl;

// @ExtendWith: integrates Mockito with JUnit 5 for this test class.
// It initializes @Mock fields and injects them into @InjectMocks automatically.
// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
// this annotation tells JUnit to use the Mockito extension, which processes Mockito annotations like @Mock and @InjectMocks in this test class. It allows us to create mock objects and inject them into the class under test without needing to manually initialize them.
// i.e., it enables the use of Mockito's mocking capabilities in this test class, allowing us to create mock dependencies and verify interactions with them.

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

        private MockMvc mockMvc;

        private ObjectMapper objectMapper;

        /*
         * @Mock: creates a mocked TaskServiceImpl dependency.
         * This keeps controller tests isolated from service/business logic.
         * 
         * * Creates a Mockito mock for isolating dependencies.
         * this creates a mock instance of the TaskServiceImpl class, which is a
         * dependency of the TaskController. The mock will be used to simulate the
         * behavior of the service layer without invoking actual business logic or
         * database interactions. This allows us to test the controller in isolation and
         * verify that it interacts with the service as expected.
         */
        @Mock
        private TaskServiceImpl taskService;

        // @InjectMocks: creates controller and injects mock dependencies.
        /* this creates an instance of the TaskController class and injects the mocked
        dependencies (like taskService) into it. This allows us to test the
        TaskController with its dependencies mocked, ensuring that we can verify
        interactions and responses without relying on real implementations of the
        service layer. */
        @InjectMocks
        private TaskController taskController;

        // @BeforeEach: runs setup before every test method.
        @BeforeEach
        // Setup: create shared fixtures/mocks so each test runs in a predictable state.
        void setUp() {
                objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
        }

        // @Test: marks this method as a JUnit test.
        @Test
        // Test: runs the createTask_returnsCreatedAndBody scenario and checks expected
        // outputs/side effects.
        // this test verifies that when a POST request is made to create a new task, the
        // controller returns a 201 Created status and the expected response body. It
        // also captures the request sent to the service layer to ensure it contains the
        // correct data.
        void createTask_returnsCreatedAndBody() throws Exception {
                LocalDateTime createdDate = LocalDateTime.of(2026, 2, 6, 9, 15);

                TaskRequestDto request = TaskRequestDto.builder()
                                .title("Investigate outage")
                                .description("Check service logs")
                                .assignedTo(200L)
                                .incidentId(300L)
                                .build();

                TaskResponseDto response = TaskResponseDto.builder()
                                .taskId(1L)
                                .title("Investigate outage")
                                .description("Check service logs")
                                .status(TaskStatus.PENDING)
                                .dueDate(LocalDateTime.of(2026, 2, 7, 12, 30))
                                .createdDate(createdDate)
                                .assignedTo(200L)
                                .assignedBy(101L)
                                .incidentId(300L)
                                .build();

                when(taskService.createTask(any(TaskRequestDto.class))).thenReturn(response);

                // Executes POST /api/tasks and verifies expected response payload.
                /*  Perform the POST request to create a new task and verify the response.
                it sends a POST request to the /api/tasks endpoint with the task details in
                the request body. It then checks that the response status is 201 Created and
                that the response body contains the expected task details (like taskId,
                title, status, etc.). This ensures that the controller correctly handles task
                creation requests and returns the appropriate response. */
                mockMvc.perform(post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.taskId").value(1L))
                                .andExpect(jsonPath("$.title").value("Investigate outage"))
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andExpect(jsonPath("$.assignedTo").value(200L))
                                .andExpect(jsonPath("$.incidentId").value(300L));

                // Captures the DTO passed to service and verifies mapped request fields.
             /*    this captures the TaskRequestDto object that was passed to the
                taskService.createTask method. It then asserts that the captured request
                contains the expected title, assignedTo, and incidentId values. This ensures
                that the controller is correctly mapping the incoming request data to the
                service layer's expected input. */
                ArgumentCaptor<TaskRequestDto> captor = ArgumentCaptor.forClass(TaskRequestDto.class);
                verify(taskService).createTask(captor.capture());
                TaskRequestDto captured = captor.getValue();
                org.junit.jupiter.api.Assertions.assertEquals("Investigate outage", captured.getTitle());
                org.junit.jupiter.api.Assertions.assertEquals(200L, captured.getAssignedTo());
                org.junit.jupiter.api.Assertions.assertEquals(300L, captured.getIncidentId());
        }

        @Test
        // Test: runs the getAllTasks_returnsOkAndList scenario and checks expected
        // outputs/side effects.
        void getAllTasks_returnsOkAndList() throws Exception {
                TaskResponseDto t1 = TaskResponseDto.builder()
                                .taskId(1L)
                                .title("T1")
                                .status(TaskStatus.PENDING)
                                .build();
                TaskResponseDto t2 = TaskResponseDto.builder()
                                .taskId(2L)
                                .title("T2")
                                .status(TaskStatus.IN_PROGRESS)
                                .build();

                when(taskService.getAllTasks()).thenReturn(List.of(t1, t2));

                mockMvc.perform(get("/api/tasks"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[0].taskId").value(1L))
                                .andExpect(jsonPath("$[1].taskId").value(2L));

                verify(taskService).getAllTasks();
        }

        @Test
        // Test: runs the getTaskByTaskId_returnsOk scenario and checks expected
        // outputs/side effects.
        void getTaskByTaskId_returnsOk() throws Exception {
                TaskResponseDto response = TaskResponseDto.builder()
                                .taskId(10L)
                                .title("Task 10")
                                .status(TaskStatus.PENDING)
                                .build();

                when(taskService.getTaskByTaskId(10L)).thenReturn(response);

                mockMvc.perform(get("/api/tasks/10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.taskId").value(10L))
                                .andExpect(jsonPath("$.title").value("Task 10"));

                verify(taskService).getTaskByTaskId(10L);
        }

        @Test
        // Test: runs the getTaskByIncidentId_returnsOk scenario and checks expected
        // outputs/side effects.
        void getTaskByIncidentId_returnsOk() throws Exception {
                TaskResponseDto response = TaskResponseDto.builder()
                                .taskId(11L)
                                .title("Incident task")
                                .status(TaskStatus.PENDING)
                                .build();

                when(taskService.getTaskByIncidentId(77L)).thenReturn(List.of(response));

                mockMvc.perform(get("/api/tasks/incident/77"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].taskId").value(11L));

                verify(taskService).getTaskByIncidentId(77L);
        }

        @Test
        // Test: runs the getTaskByAssignedTo_returnsOk scenario and checks expected
        // outputs/side effects.
        void getTaskByAssignedTo_returnsOk() throws Exception {
                TaskResponseDto response = TaskResponseDto.builder()
                                .taskId(12L)
                                .title("Assigned to user")
                                .status(TaskStatus.IN_PROGRESS)
                                .build();

                when(taskService.getTaskByAssignedTo(500L)).thenReturn(List.of(response));

                mockMvc.perform(get("/api/tasks/assignedTo/500"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].taskId").value(12L));

                verify(taskService).getTaskByAssignedTo(500L);
        }

        @Test
        // Test: runs the getTaskAssignedToMe_returnsOk scenario and checks expected
        // outputs/side effects.
        void getTaskAssignedToMe_returnsOk() throws Exception {
                TaskResponseDto response = TaskResponseDto.builder()
                                .taskId(20L)
                                .title("My task")
                                .status(TaskStatus.IN_PROGRESS)
                                .build();

                when(taskService.getTaskAssigenedToMe()).thenReturn(List.of(response));

                mockMvc.perform(get("/api/tasks/assignedToMe"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].taskId").value(20L));

                verify(taskService).getTaskAssigenedToMe();
        }

        @Test
        // Test: runs the getTaskByAssignedBy_returnsOk scenario and checks expected
        // outputs/side effects.
        void getTaskByAssignedBy_returnsOk() throws Exception {
                TaskResponseDto response = TaskResponseDto.builder()
                                .taskId(13L)
                                .title("Assigned by manager")
                                .status(TaskStatus.PENDING)
                                .build();

                when(taskService.getTaskByAssignedBy(900L)).thenReturn(List.of(response));

                mockMvc.perform(get("/api/tasks/assignedBy/900"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].taskId").value(13L));

                verify(taskService).getTaskByAssignedBy(900L);
        }

        @Test
        // Test: runs the getTaskAssignedByMe_returnsOk scenario and checks expected
        // outputs/side effects.
        void getTaskAssignedByMe_returnsOk() throws Exception {
                TaskResponseDto response = TaskResponseDto.builder()
                                .taskId(21L)
                                .title("My assigned task")
                                .status(TaskStatus.PENDING)
                                .build();

                when(taskService.getTaskByAssignedByMe()).thenReturn(List.of(response));

                mockMvc.perform(get("/api/tasks/assignedByMe"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].taskId").value(21L));

                verify(taskService).getTaskByAssignedByMe();
        }

        @Test
        // Test: runs the updateStatus_returnsOkAndMessage scenario and checks expected
        // outputs/side effects.
        void updateStatus_returnsOkAndMessage() throws Exception {
                TaskStatusUpdateRequestDto request = new TaskStatusUpdateRequestDto("COMPLETED");

                mockMvc.perform(patch("/api/tasks/42/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Task status updated"));

                verify(taskService).updateTaskStatus(42L, "COMPLETED");
        }
}
