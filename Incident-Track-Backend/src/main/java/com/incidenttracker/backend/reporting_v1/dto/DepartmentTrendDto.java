package com.incidenttracker.backend.reporting_v1.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DepartmentTrendDto {
    private Long departmentId;
    private String departmentName;
    private List<TrendPointDto> series;
}

