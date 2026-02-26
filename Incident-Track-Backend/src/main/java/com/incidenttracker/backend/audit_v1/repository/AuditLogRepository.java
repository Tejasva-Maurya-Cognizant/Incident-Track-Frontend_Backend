package com.incidenttracker.backend.audit_v1.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.incidenttracker.backend.audit_v1.entity.AuditLog;
import com.incidenttracker.backend.common.enums.ActionType;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByIncident_IncidentIdOrderByTimestampDesc(Long incidentId);

    List<AuditLog> findByActionTypeOrderByTimestampDesc(ActionType actionType);

    // ---- Pageable versions ----
    Page<AuditLog> findAll(Pageable pageable);

    Page<AuditLog> findByIncident_IncidentId(Long incidentId, Pageable pageable);

    Page<AuditLog> findByActionType(ActionType actionType, Pageable pageable);
}
