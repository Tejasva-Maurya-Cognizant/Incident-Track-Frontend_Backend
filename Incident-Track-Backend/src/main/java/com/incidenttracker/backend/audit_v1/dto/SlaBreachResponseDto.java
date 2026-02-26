package com.incidenttracker.backend.audit_v1.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class SlaBreachResponseDto {
    private Long breachId;

    private Long incidentId;
    private String incidentStatus;

    private LocalDateTime slaDueAt;
    private LocalDateTime breachedAt;
    private Long breachMinutes;

    private String breachStatus;
    private String reason;
}
