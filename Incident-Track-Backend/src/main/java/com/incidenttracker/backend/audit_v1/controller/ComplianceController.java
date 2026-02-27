package com.incidenttracker.backend.audit_v1.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.incidenttracker.backend.audit_v1.dto.AuditLogResponseDto;
import com.incidenttracker.backend.audit_v1.dto.SlaBreachResponseDto;
import com.incidenttracker.backend.audit_v1.entity.AuditLog;
import com.incidenttracker.backend.audit_v1.entity.IncidentSlaBreach;
import com.incidenttracker.backend.audit_v1.repository.AuditLogRepository;
import com.incidenttracker.backend.audit_v1.repository.IncidentSlaBreachRepository;
import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.common.enums.ActionType;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/compliance")
public class ComplianceController {

        private final AuditLogRepository auditLogRepository;
        private final IncidentSlaBreachRepository breachRepository;

        // Non-paged
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/audit-logs")
        public List<AuditLogResponseDto> getAllAuditLogs() {
                return auditLogRepository.findAll()
                                .stream()
                                .map(this::mapAuditToDto)
                                .toList();
        }

        /**
         * GET
         * /api/admin/compliance/audit-logs/paged?page=0&size=20&sortBy=timestamp&sortDir=desc
         */
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/audit-logs/paged")
        public ResponseEntity<PagedResponse<AuditLogResponseDto>> getAllAuditLogsPaged(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "timestamp") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                String resolvedSortBy = switch (sortBy) {
                        case "incidentId" -> "incident.incidentId";
                        case "username" -> "user.username";
                        default -> sortBy;
                };
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(resolvedSortBy).ascending()
                                : Sort.by(resolvedSortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<AuditLog> auditPage = auditLogRepository.findAll(pageable);
                PagedResponse<AuditLogResponseDto> response = PagedResponse.<AuditLogResponseDto>builder()
                                .content(auditPage.getContent().stream().map(this::mapAuditToDto).toList())
                                .page(auditPage.getNumber())
                                .size(auditPage.getSize())
                                .totalElements(auditPage.getTotalElements())
                                .totalPages(auditPage.getTotalPages())
                                .last(auditPage.isLast())
                                .first(auditPage.isFirst())
                                .build();
                return ResponseEntity.ok(response);
        }

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/audit-logs/{incidentId}")
        public List<AuditLogResponseDto> getAuditLogsByIncident(@PathVariable Long incidentId) {
                return auditLogRepository.findByIncident_IncidentIdOrderByTimestampDesc(incidentId)
                                .stream()
                                .map(this::mapAuditToDto)
                                .toList();
        }

        /**
         * GET /api/admin/compliance/audit-logs/{incidentId}/paged
         */
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/audit-logs/{incidentId}/paged")
        public ResponseEntity<PagedResponse<AuditLogResponseDto>> getAuditLogsByIncidentPaged(
                        @PathVariable Long incidentId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "timestamp") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<AuditLog> auditPage = auditLogRepository.findByIncident_IncidentId(incidentId, pageable);
                PagedResponse<AuditLogResponseDto> response = PagedResponse.<AuditLogResponseDto>builder()
                                .content(auditPage.getContent().stream().map(this::mapAuditToDto).toList())
                                .page(auditPage.getNumber())
                                .size(auditPage.getSize())
                                .totalElements(auditPage.getTotalElements())
                                .totalPages(auditPage.getTotalPages())
                                .last(auditPage.isLast())
                                .first(auditPage.isFirst())
                                .build();
                return ResponseEntity.ok(response);
        }

        // Non-paged
        @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
        @GetMapping("/breaches")
        public List<SlaBreachResponseDto> getAllBreaches() {
                return breachRepository.findAll()
                                .stream()
                                .map(this::mapBreachToDto)
                                .toList();
        }

        /**
         * GET
         * /api/admin/compliance/breaches/paged?page=0&size=20&sortBy=breachedAt&sortDir=desc
         */
        @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
        @GetMapping("/breaches/paged")
        public ResponseEntity<PagedResponse<SlaBreachResponseDto>> getAllBreachesPaged(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "breachedAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                // Map frontend field names to JPA traversal paths where needed
                String resolvedSortBy = switch (sortBy) {
                        case "incidentId" -> "incident.incidentId";
                        case "incidentStatus" -> "incident.status";
                        default -> sortBy;
                };
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(resolvedSortBy).ascending()
                                : Sort.by(resolvedSortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<IncidentSlaBreach> breachPage = breachRepository.findAll(pageable);
                PagedResponse<SlaBreachResponseDto> response = PagedResponse.<SlaBreachResponseDto>builder()
                                .content(breachPage.getContent().stream().map(this::mapBreachToDto).toList())
                                .page(breachPage.getNumber())
                                .size(breachPage.getSize())
                                .totalElements(breachPage.getTotalElements())
                                .totalPages(breachPage.getTotalPages())
                                .last(breachPage.isLast())
                                .first(breachPage.isFirst())
                                .build();
                return ResponseEntity.ok(response);
        }

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/audit-log/{actionType}")
        public List<AuditLogResponseDto> getAuditLogsByActionType(@PathVariable String actionType) {
                ActionType action = ActionType.valueOf(actionType.toUpperCase());
                return auditLogRepository.findByActionTypeOrderByTimestampDesc(action)
                                .stream()
                                .map(this::mapAuditToDto)
                                .toList();
        }

        /**
         * GET /api/admin/compliance/audit-log/{actionType}/paged
         */
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/audit-log/{actionType}/paged")
        public ResponseEntity<PagedResponse<AuditLogResponseDto>> getAuditLogsByActionTypePaged(
                        @PathVariable String actionType,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "timestamp") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                ActionType action = ActionType.valueOf(actionType.toUpperCase());
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<AuditLog> auditPage = auditLogRepository.findByActionType(action, pageable);
                PagedResponse<AuditLogResponseDto> response = PagedResponse.<AuditLogResponseDto>builder()
                                .content(auditPage.getContent().stream().map(this::mapAuditToDto).toList())
                                .page(auditPage.getNumber())
                                .size(auditPage.getSize())
                                .totalElements(auditPage.getTotalElements())
                                .totalPages(auditPage.getTotalPages())
                                .last(auditPage.isLast())
                                .first(auditPage.isFirst())
                                .build();
                return ResponseEntity.ok(response);
        }

        private AuditLogResponseDto mapAuditToDto(AuditLog log) {
                return AuditLogResponseDto.builder()
                                .logId(log.getLogId())
                                .incidentId(log.getIncident() != null ? log.getIncident().getIncidentId() : null)
                                .userId(log.getUser() != null ? log.getUser().getUserId() : null)
                                .username(log.getUser() != null ? log.getUser().getUsername() : null)
                                .actionType(log.getActionType().name())
                                .timestamp(log.getTimestamp())
                                .details(log.getDetails())
                                .build();
        }

        private SlaBreachResponseDto mapBreachToDto(IncidentSlaBreach breach) {
                return SlaBreachResponseDto.builder()
                                .breachId(breach.getBreachId())
                                .incidentId(breach.getIncident().getIncidentId())
                                .incidentStatus(breach.getIncident().getStatus().name())
                                .slaDueAt(breach.getSlaDueAt())
                                .breachedAt(breach.getBreachedAt())
                                .breachMinutes(breach.getBreachMinutes())
                                .breachStatus(breach.getBreachStatus().name())
                                .reason(breach.getReason())
                                .build();
        }
}
