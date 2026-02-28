package com.incidenttracker.backend.incident.entity;

import java.time.LocalDateTime;

import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long incidentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false) // This creates the FK in the 'incidents' table
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentSeverity calculatedSeverity;

    @Column(nullable = false)
    private Boolean isCritical;

    @Column(nullable = false, updatable = false)
    private LocalDateTime reportedDate;

    private LocalDateTime resolvedDate;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;


    @Column(nullable = false)
    private Boolean slaBreached = false;

    private LocalDateTime slaDueAt;


    @PrePersist
    protected void onCreate() {
        this.reportedDate = LocalDateTime.now();
        this.status = IncidentStatus.OPEN;
        // this.userMarkedCritical=Boolean.FALSE;
        if (this.isCritical == null) {
            this.isCritical = false;
        }
        if (this.slaBreached == null) this.slaBreached = false;

        // SLA due date from category.slaTimeHours
        if (this.category != null && this.category.getSlaTimeHours() != null) {
            if (this.isCritical){
                this.slaDueAt = (this.reportedDate.plusHours(2));
            }
            else {
            this.slaDueAt = this.reportedDate.plusHours(this.category.getSlaTimeHours());
            }
        }
    }
}
