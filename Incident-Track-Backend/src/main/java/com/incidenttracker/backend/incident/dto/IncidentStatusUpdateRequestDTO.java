package com.incidenttracker.backend.incident.dto;

import com.incidenttracker.backend.common.enums.IncidentStatus;

import lombok.*;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
public class IncidentStatusUpdateRequestDTO {
    private IncidentStatus status;
    private String note; // optional: why status changed
}

