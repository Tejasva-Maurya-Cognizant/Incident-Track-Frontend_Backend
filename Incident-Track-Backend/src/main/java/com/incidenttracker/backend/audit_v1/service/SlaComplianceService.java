package com.incidenttracker.backend.audit_v1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.incidenttracker.backend.audit_v1.entity.IncidentSlaBreach;
import com.incidenttracker.backend.audit_v1.repository.IncidentSlaBreachRepository;
import com.incidenttracker.backend.common.enums.ActionType;
import com.incidenttracker.backend.common.enums.BreachStatus;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.incident.repository.IncidentRepository;
import com.incidenttracker.backend.notification.service.NotificationService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlaComplianceService {

    private final IncidentRepository incidentRepository;
    private final IncidentSlaBreachRepository breachRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    // runs every 5 minutes
    // @Scheduled(fixedDelay = 5 * 1000) // 5 seconds
    @Scheduled(fixedDelay = 5 * 60 * 1000) // 5 minutes
    @Transactional
    public void detectSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();

        List<Incident> overdue = incidentRepository.findSlaOverdueNotMarked(now);

        overdue.forEach(o -> System.out.println(o));

        for (Incident incident : overdue) {

            // Safety: if breach already exists, skip. The scheduler reads the already
            // calculated effective SLA deadline from incident.slaDueAt.
            if (breachRepository.existsByIncident_IncidentId(incident.getIncidentId())) {
                incident.setSlaBreached(true);
                continue;
            }

            LocalDateTime due = incident.getSlaDueAt();
            long minutesLate = Duration.between(due, now).toMinutes();

            IncidentSlaBreach breach = IncidentSlaBreach.builder()
                    .incident(incident)
                    .slaDueAt(due)
                    .breachedAt(now)
                    .breachMinutes(minutesLate)
                    .breachStatus(BreachStatus.OPEN)
                    .reason("Incident not resolved within the effective SLA deadline")
                    .build();

            breachRepository.save(breach);

            incident.setSlaBreached(true); // mark incident
            Incident saved = incidentRepository.save(incident);
            notificationService.notifySlaBreached(saved);

            // audit log entry for breach
            auditService.log(incident, null, ActionType.INCIDENT_UPDATED,
                    "SLA BREACH detected. DueAt= " + due + ", breachedAt=" + now + ", lateMinutes= " + minutesLate);
        }
    }
}
