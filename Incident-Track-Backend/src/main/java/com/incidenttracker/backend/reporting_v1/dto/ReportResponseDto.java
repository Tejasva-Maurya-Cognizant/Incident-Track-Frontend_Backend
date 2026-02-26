package com.incidenttracker.backend.reporting_v1.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReportResponseDto {
    private Long reportId;
    private String reportType;
    private String scope;
    private Long scopeRefId;

    private Long incidentCount;
    private Long resolvedIncidentCount;
    private Long slaBreachedCount;
    private Double slaComplianceRate;
    private Double averageResolutionTimeHours;

    private LocalDate startDate;
    private LocalDate endDate;

    private Object series; // TrendPointDTO list OR department trend list
    private LocalDateTime generatedAt;
}

