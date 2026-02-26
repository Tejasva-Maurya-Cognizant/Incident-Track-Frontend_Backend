package com.incidenttracker.backend.audit_v1.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class AuditLogResponseDto {
    private Long logId;

    private Long incidentId;
    private Long userId;
    private String username;

    private String actionType;
    private LocalDateTime timestamp;
    private String details;
}
