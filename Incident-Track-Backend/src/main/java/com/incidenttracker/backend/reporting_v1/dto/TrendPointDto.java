package com.incidenttracker.backend.reporting_v1.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TrendPointDto {
    private String label;          // "2026-02-07" or "2026-02"
    private Long incidentCount;
    private Long slaBreachedCount;
}

