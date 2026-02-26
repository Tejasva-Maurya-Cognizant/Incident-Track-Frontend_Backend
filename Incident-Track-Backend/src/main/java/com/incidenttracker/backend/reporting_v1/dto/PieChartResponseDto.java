package com.incidenttracker.backend.reporting_v1.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PieChartResponseDto {
    private String title;
    private List<PieSliceDto> data;
}
