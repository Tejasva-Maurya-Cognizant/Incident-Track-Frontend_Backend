package com.incidenttracker.backend.reporting_v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DepartmentPerformanceDto {
    private Long departmentId;
    private String departmentName;

    private Long incidentCount;
    private Long resolvedIncidentCount;
    private Long slaBreachedCount;
    private Double slaComplianceRate;
    private Double averageResolutionTimeHours;
}
