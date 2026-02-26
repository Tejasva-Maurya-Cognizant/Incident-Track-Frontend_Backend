package com.incidenttracker.backend.incident.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class IncidentRequestDTO {
	private Long categoryId;

	private String description;

	// user manually marks critical if urgency is high
	private Boolean isCritical;
}
