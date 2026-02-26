package com.incidenttracker.backend.incident.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.incident.dto.IncidentRequestDTO;
import com.incidenttracker.backend.incident.dto.IncidentResponseDTO;
import com.incidenttracker.backend.incident.dto.IncidentStatusUpdateRequestDTO;
import com.incidenttracker.backend.incident.service.IncidentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/incidents")
public class IncidentController {

        private final IncidentService incidentService;

        // creating incident
        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
        @PostMapping()
        public ResponseEntity<IncidentResponseDTO> createIncident(@RequestBody IncidentRequestDTO requestDTO) {
                IncidentResponseDTO response = incidentService.createIncident(requestDTO);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        // get all incidents of a user (non-paged)
        @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'EMPLOYEE')")
        @GetMapping()
        public ResponseEntity<List<IncidentResponseDTO>> getIncidentsByUser() {
                List<IncidentResponseDTO> incidents = incidentService.getIncidentsUser();
                return ResponseEntity
                                .ok()
                                .body(incidents);
        }

        /**
         * GET /api/incidents/paged?page=0&size=10&sortBy=reportedDate&sortDir=desc
         * Returns a paginated, sorted list of the current user's incidents.
         */
        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
        @GetMapping("/paged")
        public ResponseEntity<PagedResponse<IncidentResponseDTO>> getIncidentsByUserPaged(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "reportedDate") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                return ResponseEntity.ok(incidentService.getIncidentsUserPaged(pageable));
        }

        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
        @GetMapping("/status/{status}")
        public ResponseEntity<List<IncidentResponseDTO>> getIncidentsByStatus(
                        @PathVariable IncidentStatus status) {
                List<IncidentResponseDTO> incidents = incidentService.getIncidentsByUserAndStatus(status);
                return ResponseEntity
                                .ok()
                                .body(incidents);
        }

        /**
         * GET
         * /api/incidents/status/{status}/paged?page=0&size=10&sortBy=reportedDate&sortDir=desc
         */
        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
        @GetMapping("/status/{status}/paged")
        public ResponseEntity<PagedResponse<IncidentResponseDTO>> getIncidentsByStatusPaged(
                        @PathVariable IncidentStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "reportedDate") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                return ResponseEntity.ok(incidentService.getIncidentsByUserAndStatusPaged(status, pageable));
        }

        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
        @GetMapping("/calculatedSeverity/{calculatedSeverity}")
        public ResponseEntity<List<IncidentResponseDTO>> getIncidentsByCalculatedSeverity(
                        @PathVariable IncidentSeverity calculatedSeverity) {
                List<IncidentResponseDTO> incidents = incidentService
                                .getIncidentsByUserAndCalculatedSeverity(calculatedSeverity);
                return ResponseEntity
                                .ok()
                                .body(incidents);
        }

        /**
         * GET /api/incidents/calculatedSeverity/{calculatedSeverity}/paged
         */
        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
        @GetMapping("/calculatedSeverity/{calculatedSeverity}/paged")
        public ResponseEntity<PagedResponse<IncidentResponseDTO>> getIncidentsBySeverityPaged(
                        @PathVariable IncidentSeverity calculatedSeverity,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "reportedDate") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                return ResponseEntity.ok(incidentService
                                .getIncidentsByUserAndCalculatedSeverityPaged(calculatedSeverity, pageable));
        }

        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
        @GetMapping("/CRITICAL")
        public ResponseEntity<List<IncidentResponseDTO>> getIncidentsByIsCritical() {
                List<IncidentResponseDTO> incidents = incidentService
                                .getIncidentsByUserAndUserMarkedCritical(Boolean.TRUE);
                return ResponseEntity
                                .ok()
                                .body(incidents);
        }

        /**
         * GET /api/incidents/CRITICAL/paged
         */
        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
        @GetMapping("/CRITICAL/paged")
        public ResponseEntity<PagedResponse<IncidentResponseDTO>> getIncidentsByCriticalPaged(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "reportedDate") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                return ResponseEntity.ok(
                                incidentService.getIncidentsByUserAndUserMarkedCriticalPaged(Boolean.TRUE, pageable));
        }

        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
        @GetMapping("/{incidentId}")
        public ResponseEntity<IncidentResponseDTO> getIncidentDetails(@PathVariable Long incidentId) {
                IncidentResponseDTO incidents = incidentService.getIncidentDetails(incidentId);
                return ResponseEntity
                                .ok()
                                .body(incidents);
        }

        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
        @PutMapping("/withdraw/{incidentId}")
        public ResponseEntity<IncidentResponseDTO> withdrawIncident(@PathVariable Long incidentId) {
                IncidentResponseDTO incidents = incidentService.withdrawIncident(incidentId);
                return ResponseEntity
                                .ok()
                                .body(incidents);
        }

        @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
        @GetMapping("/admin-manager/all")
        public ResponseEntity<List<IncidentResponseDTO>> getAllIncidentsForAdmin() {
                List<IncidentResponseDTO> incidents = incidentService.getAllIncidents();
                return ResponseEntity
                                .ok()
                                .body(incidents);
        }

        /**
         * GET
         * /api/incidents/admin-manager/all/paged?page=0&size=10&sortBy=reportedDate&sortDir=desc
         */
        @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
        @GetMapping("/admin-manager/all/paged")
        public ResponseEntity<PagedResponse<IncidentResponseDTO>> getAllIncidentsForAdminPaged(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "reportedDate") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                return ResponseEntity.ok(incidentService.getAllIncidentsPaged(pageable));
        }

        @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
        @GetMapping("/admin-manager/{incidentId}")
        public ResponseEntity<IncidentResponseDTO> getIncidentDetailsForAdmin(@PathVariable Long incidentId) {
                IncidentResponseDTO incidents = incidentService.getIncidentDetailsForAdmin(incidentId);
                return ResponseEntity
                                .ok()
                                .body(incidents);
        }

        @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
        @PutMapping("/{incidentId}/status")
        public ResponseEntity<IncidentResponseDTO> updateStatus(
                        @PathVariable Long incidentId,
                        @RequestBody IncidentStatusUpdateRequestDTO request) {
                IncidentResponseDTO incidents = incidentService.updateIncidentStatus(incidentId, request);

                return ResponseEntity.ok().body(incidents);
        }

}