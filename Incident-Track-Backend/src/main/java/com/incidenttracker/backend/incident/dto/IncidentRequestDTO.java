package com.incidenttracker.backend.incident.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

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

	// User can mark the incident as urgent; legacy "isCritical" is still accepted.
	@JsonAlias("isCritical")
	private Boolean urgent;
}
