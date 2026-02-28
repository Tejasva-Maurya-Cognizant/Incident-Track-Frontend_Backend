package com.incidenttracker.backend.incident.service;

import org.springframework.data.domain.Pageable;

import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.incident.dto.IncidentRequestDTO;
import com.incidenttracker.backend.incident.dto.IncidentResponseDTO;
import com.incidenttracker.backend.incident.dto.IncidentStatusUpdateRequestDTO;

import java.util.List;

public interface IncidentService {
	IncidentResponseDTO createIncident(IncidentRequestDTO dto);

	List<IncidentResponseDTO> getIncidentsUser();

	List<IncidentResponseDTO> getIncidentsByUserAndStatus(IncidentStatus status);

	List<IncidentResponseDTO> getIncidentsByUserAndCalculatedSeverity(IncidentSeverity calculatedSeverity);

	List<IncidentResponseDTO> getIncidentsByUserAndUrgent(Boolean urgent);

	List<IncidentResponseDTO> getAllIncidents();

	IncidentResponseDTO getIncidentDetails(Long userId);

	IncidentResponseDTO getIncidentDetailsForAdmin(Long incidentId);

	IncidentResponseDTO withdrawIncident(Long userId);

	IncidentResponseDTO updateIncidentStatus(Long incidentId, IncidentStatusUpdateRequestDTO request);

	// ---- Paginated versions ----
	PagedResponse<IncidentResponseDTO> getIncidentsUserPaged(Pageable pageable);

	PagedResponse<IncidentResponseDTO> getIncidentsByUserAndStatusPaged(IncidentStatus status, Pageable pageable);

	PagedResponse<IncidentResponseDTO> getIncidentsByUserAndCalculatedSeverityPaged(IncidentSeverity calculatedSeverity,
			Pageable pageable);

	PagedResponse<IncidentResponseDTO> getIncidentsByUserAndUrgentPaged(Boolean urgent,
			Pageable pageable);

	PagedResponse<IncidentResponseDTO> getAllIncidentsPaged(Pageable pageable);

	PagedResponse<IncidentResponseDTO> getAllIncidentsByStatusPaged(IncidentStatus status, Pageable pageable);
}
