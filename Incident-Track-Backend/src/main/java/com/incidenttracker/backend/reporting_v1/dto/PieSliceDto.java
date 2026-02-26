package com.incidenttracker.backend.reporting_v1.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PieSliceDto {
    private String label;  // e.g., "IT", "HR", "Network"
    private Long value; // count
}

