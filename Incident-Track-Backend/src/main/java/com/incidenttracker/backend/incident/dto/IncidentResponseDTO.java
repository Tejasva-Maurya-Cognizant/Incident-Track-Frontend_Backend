package com.incidenttracker.backend.incident.dto;

import java.time.LocalDateTime;

import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.IncidentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponseDTO {
	private Long incidentId;
	private Long categoryId;
	private String description;
	private Long userId;
	private String username;
	private IncidentStatus status;
	private IncidentSeverity calculatedSeverity;
	private Boolean isCritical;
	private LocalDateTime reportedDate;
	private String categoryName;
	private String subCategory;
	private Integer slaHours;
}
