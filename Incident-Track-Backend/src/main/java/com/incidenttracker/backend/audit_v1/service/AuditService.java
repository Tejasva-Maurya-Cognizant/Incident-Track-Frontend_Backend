package com.incidenttracker.backend.audit_v1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.incidenttracker.backend.audit_v1.entity.AuditLog;
import com.incidenttracker.backend.audit_v1.repository.AuditLogRepository;
import com.incidenttracker.backend.common.enums.ActionType;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.user.entity.User;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(Incident incident, User user, ActionType actionType, String details) {
        AuditLog log = AuditLog.builder()
                .incident(incident)
                .user(user)
                .actionType(actionType)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }
}
