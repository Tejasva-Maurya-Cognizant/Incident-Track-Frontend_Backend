package com.incidenttracker.backend.audit_v1.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.incidenttracker.backend.common.enums.BreachStatus;
import com.incidenttracker.backend.incident.entity.Incident;

@Entity
@Table(name = "incident_sla_breach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentSlaBreach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long breachId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false, unique = true)
    private Incident incident;

    @Column(nullable = false)
    private LocalDateTime slaDueAt;

    @Column(nullable = false)
    private LocalDateTime breachedAt;

    @Column(nullable = false)
    private Long breachMinutes; // how late when detected

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BreachStatus breachStatus;

    @Column(length = 2000)
    private String reason; // optional: "SLA exceeded before resolution"
}
