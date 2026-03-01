package com.incidenttracker.backend.reporting_v1.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.incidenttracker.backend.common.util.DateTimeUtils;
import com.incidenttracker.backend.reporting_v1.enums.ReportScope;
import com.incidenttracker.backend.reporting_v1.enums.ReportType;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReportScope scope;

    /**
     * Reference IDs (nullable based on scope) nullable for GLOBAL
     * Example:
     * - DEPARTMENT → departmentId
     * - CATEGORY → categoryId
     */
    private Long scopeRefId;

    // 🔢 Core Metrics
    private Long incidentCount;
    private Long resolvedIncidentCount;
    private Long slaBreachedCount;
    private Double slaComplianceRate;
    private Double averageResolutionTimeHours;

    // 📅 Period info (for PERIOD reports)
    private LocalDate startDate;
    private LocalDate endDate;

    // ✅ Store computed series (trend points) as JSON (minimal DB design)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String seriesJson;    
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String metricsJson;

    // 👤 Audit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    void onCreate() {
        if (generatedAt == null) {
            generatedAt = DateTimeUtils.nowTruncatedToSeconds();
        }
    }
}

