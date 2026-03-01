package com.incidenttracker.backend.audit_v1.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.incidenttracker.backend.common.enums.ActionType;
import com.incidenttracker.backend.common.util.DateTimeUtils;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.user.entity.User;

@Entity
@Table(name = "audit_log")
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident; // can be null for non-incident actions if needed

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ActionType actionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // who performed the action

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 2000)
    private String details;

    @PrePersist
    void onCreate() {
        timestamp = DateTimeUtils.nowTruncatedToSeconds();
    }
}
