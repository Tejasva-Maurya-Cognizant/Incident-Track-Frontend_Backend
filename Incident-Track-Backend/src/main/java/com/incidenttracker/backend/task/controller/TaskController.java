package com.incidenttracker.backend.task.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.common.enums.TaskStatus;
import com.incidenttracker.backend.task.dto.TaskRequestDto;
import com.incidenttracker.backend.task.dto.TaskResponseDto;
import com.incidenttracker.backend.task.dto.TaskStatusUpdateRequestDto;
import com.incidenttracker.backend.task.service.impl.TaskServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskServiceImpl taskService;

    // Creates a task for an incident and returns 201 with created task details.
    @PreAuthorize("hasAnyRole('MANAGER')")
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto request) {
        TaskResponseDto response = taskService.createTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Returns all tasks (non-paged).
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
        List<TaskResponseDto> response = taskService.getAllTasks();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * GET /api/tasks/paged?page=0&size=10&sortBy=createdDate&sortDir=desc
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<TaskResponseDto>> getAllTasksPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(taskService.getAllTasksPaged(pageable));
    }

    // Returns one task by task id.
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> getTaskByTaskId(@PathVariable("taskId") Long taskId) {
        TaskResponseDto response = taskService.getTaskByTaskId(taskId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Returns all tasks linked to one incident (non-paged).
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/incident/{incidentId}")
    public ResponseEntity<List<TaskResponseDto>> getTaskByIncidentId(@PathVariable("incidentId") Long incidentId) {
        List<TaskResponseDto> response = taskService.getTaskByIncidentId(incidentId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * GET /api/tasks/incident/{incidentId}/paged
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/incident/{incidentId}/paged")
    public ResponseEntity<PagedResponse<TaskResponseDto>> getTaskByIncidentIdPaged(
            @PathVariable("incidentId") Long incidentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(taskService.getTaskByIncidentIdPaged(incidentId, pageable));
    }

    // Returns tasks assigned to a specific user id (non-paged).
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/assignedTo/{assignedTo}")
    public ResponseEntity<List<TaskResponseDto>> getTaskByAssignedTo(@PathVariable("assignedTo") Long assignedTo) {
        List<TaskResponseDto> response = taskService.getTaskByAssignedTo(assignedTo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * GET /api/tasks/assignedTo/{assignedTo}/paged
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/assignedTo/{assignedTo}/paged")
    public ResponseEntity<PagedResponse<TaskResponseDto>> getTaskByAssignedToPaged(
            @PathVariable("assignedTo") Long assignedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(taskService.getTaskByAssignedToPaged(assignedTo, pageable));
    }

    // Returns tasks assigned to the currently authenticated employee (non-paged).
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/assignedToMe")
    public ResponseEntity<List<TaskResponseDto>> getTaskAssigenedToMe() {
        List<TaskResponseDto> response = taskService.getTaskAssigenedToMe();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * GET /api/tasks/assignedToMe/paged
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/assignedToMe/paged")
    public ResponseEntity<PagedResponse<TaskResponseDto>> getTaskAssignedToMePaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(taskService.getTaskAssignedToMePaged(pageable));
    }

    // Returns tasks created by a specific assigner user id (non-paged).
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/assignedBy/{assignedBy}")
    public ResponseEntity<List<TaskResponseDto>> getTaskByAssignedBy(@PathVariable("assignedBy") Long assignedBy) {
        List<TaskResponseDto> response = taskService.getTaskByAssignedBy(assignedBy);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * GET /api/tasks/assignedBy/{assignedBy}/paged
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/assignedBy/{assignedBy}/paged")
    public ResponseEntity<PagedResponse<TaskResponseDto>> getTaskByAssignedByPaged(
            @PathVariable("assignedBy") Long assignedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(taskService.getTaskByAssignedByPaged(assignedBy, pageable));
    }

    // Returns tasks created by the currently authenticated manager (non-paged).
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/assignedByMe")
    public ResponseEntity<List<TaskResponseDto>> getTaskByAssignedByMe() {
        List<TaskResponseDto> response = taskService.getTaskByAssignedByMe();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * GET /api/tasks/assignedByMe/paged
     */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/assignedByMe/paged")
    public ResponseEntity<PagedResponse<TaskResponseDto>> getTaskByAssignedByMePaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(taskService.getTaskByAssignedByMePaged(pageable));
    }

    /**
     * GET /api/tasks/status/{status}/paged
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/status/{status}/paged")
    public ResponseEntity<PagedResponse<TaskResponseDto>> getTaskByStatusPaged(
            @PathVariable TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(taskService.getTaskByStatusPaged(status, pageable));
    }

    // Updates task status based on role-driven workflow checks in service layer.
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long taskId,
            @RequestBody TaskStatusUpdateRequestDto request) {
        taskService.updateTaskStatus(taskId, request.status());
        return ResponseEntity.ok("Incident status updated");
    }

}
