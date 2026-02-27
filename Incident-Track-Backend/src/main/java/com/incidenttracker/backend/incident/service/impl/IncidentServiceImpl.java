package com.incidenttracker.backend.incident.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.incidenttracker.backend.audit_v1.repository.IncidentSlaBreachRepository;
import com.incidenttracker.backend.audit_v1.service.AuditService;
import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.category.repository.CategoryRepository;
import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.common.enums.ActionType;
import com.incidenttracker.backend.common.enums.BreachStatus;
import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.common.security.SecurityService;
import com.incidenttracker.backend.incident.dto.IncidentRequestDTO;
import com.incidenttracker.backend.incident.dto.IncidentResponseDTO;
import com.incidenttracker.backend.incident.dto.IncidentStatusUpdateRequestDTO;
import com.incidenttracker.backend.incident.entity.Incident;

import com.incidenttracker.backend.incident.repository.IncidentRepository;
import com.incidenttracker.backend.incident.service.IncidentService;
import com.incidenttracker.backend.notification.service.NotificationService;
import com.incidenttracker.backend.user.entity.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

	private final IncidentRepository incidentRepository;
	private final CategoryRepository categoryRepository;
	private final SecurityService securityService;
	private final IncidentSlaBreachRepository breachRepository;
	private final AuditService auditService;
	private final NotificationService notificationService;

	private IncidentSeverity calculateSeverity(Boolean isCritical, int slaHours) {
		if (Boolean.TRUE.equals(isCritical)) {
			return IncidentSeverity.CRITICAL;
		}
		if (slaHours <= 2) {
			return IncidentSeverity.CRITICAL;
		} else if (slaHours <= 6) {
			return IncidentSeverity.HIGH;
		} else if (slaHours <= 12) {
			return IncidentSeverity.MEDIUM;
		} else {
			return IncidentSeverity.LOW;
		}

	}

	@Override
	@Transactional
	public IncidentResponseDTO createIncident(IncidentRequestDTO dto) {

		Category category = categoryRepository.findById(dto.getCategoryId())
				.orElseThrow(() -> new RuntimeException("Category not found"));

		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));

		Incident incident = new Incident();
		incident.setDescription(dto.getDescription());
		incident.setReportedBy(currentUser);
		incident.setCategory(category);
		incident.setIsCritical(dto.getIsCritical());
		IncidentSeverity severity = calculateSeverity(dto.getIsCritical(), category.getSlaTimeHours());
		incident.setCalculatedSeverity(severity);

		Incident saved = incidentRepository.save(incident);

		auditService.log(saved, currentUser, ActionType.INCIDENT_CREATED, "Incident created with incident Id :- "
				+ saved.getIncidentId() + "of category :- " + saved.getCategory().getCategoryName());
		notificationService.notifyAllManager(saved);
		notificationService.notifyManagersCriticalOrCancelled(saved);
		return mapToResponseDTO(saved);
	}

	@Override
	public List<IncidentResponseDTO> getIncidentsUser() {
		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));

		return incidentRepository.findByReportedBy_UserId(currentUser.getUserId())
				.stream()
				.map(this::mapToResponseDTO)
				.toList();

	}

	@Override
	public List<IncidentResponseDTO> getIncidentsByUserAndStatus(IncidentStatus status) {
		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));
		return incidentRepository.findByReportedBy_UserIdAndStatus(currentUser.getUserId(), status)
				.stream()
				.map(this::mapToResponseDTO)
				.toList();
	}

	@Override
	public List<IncidentResponseDTO> getIncidentsByUserAndCalculatedSeverity(
			IncidentSeverity calculatedSeverity) {
		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));
		return incidentRepository
				.findByReportedBy_UserIdAndCalculatedSeverity(currentUser.getUserId(), calculatedSeverity)
				.stream()
				.map(this::mapToResponseDTO)
				.toList();
	}

	@Override
	public List<IncidentResponseDTO> getIncidentsByUserAndUserMarkedCritical(Boolean isCritical) {
		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));

		return incidentRepository.findByReportedBy_UserIdAndIsCritical(currentUser.getUserId(), isCritical)
				.stream()
				.map(this::mapToResponseDTO)
				.toList();
	}

	@Override
	public IncidentResponseDTO getIncidentDetails(Long incidentId) {
		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));
		Incident incident = incidentRepository.findByIncidentIdAndReportedBy_UserId(incidentId, currentUser.getUserId())
				.orElseThrow(() -> new RuntimeException("Incident not found or access denied"));
		return mapToResponseDTO(incident);

	}

	@Override
	public IncidentResponseDTO getIncidentDetailsForAdmin(Long incidentId) {
		Incident incident = incidentRepository.findById(incidentId)
				.orElseThrow(() -> new RuntimeException("Incident not found"));
		return mapToResponseDTO(incident);
	}

	@Override
	public IncidentResponseDTO withdrawIncident(Long incidentId) {
		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));
		Incident incident = incidentRepository.findByIncidentIdAndReportedBy_UserId(incidentId, currentUser.getUserId())
				.orElseThrow(() -> new RuntimeException("Incident not found or access denied"));

		System.out.println(currentUser.getUsername());
		if (!currentUser.getUserId().equals(incident.getReportedBy().getUserId())) {
			throw new AccessDeniedException("You are not authorized to cancel this incident.");
		}

		if (incident.getStatus() == IncidentStatus.RESOLVED || incident.getStatus() == IncidentStatus.IN_PROGRESS) {
			throw new IllegalStateException("Cannot cancel an incident that is already " + incident.getStatus());
		}

		incident.setStatus(IncidentStatus.CANCELLED);
		Incident saved = incidentRepository.save(incident);

		notificationService.notifyManagersCriticalOrCancelled(incident);

		// ✅ Auto-close breach when cancelled
		if (saved.getStatus() == IncidentStatus.CANCELLED) {

			breachRepository.findByIncident_IncidentId(incidentId).ifPresent(breach -> {
				breach.setBreachStatus(BreachStatus.RESOLVED);
				breachRepository.save(breach);

				auditService.log(saved, currentUser, ActionType.INCIDENT_WITHDRAWN,
						"Breach closed because incident moved to " + saved.getStatus());
			});
		}
		return mapToResponseDTO(saved);

	}

	@Override
	public List<IncidentResponseDTO> getAllIncidents() {
		return incidentRepository.findAll().stream().map(this::mapToResponseDTO).toList();
	}

	// ---- Paginated implementations ----

	@Override
	public PagedResponse<IncidentResponseDTO> getIncidentsUserPaged(Pageable pageable) {
		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));
		Page<Incident> page = incidentRepository.findByReportedBy_UserId(currentUser.getUserId(), pageable);
		return toPagedResponse(page);
	}

	@Override
	public PagedResponse<IncidentResponseDTO> getIncidentsByUserAndStatusPaged(IncidentStatus status,
			Pageable pageable) {
		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));
		Page<Incident> page = incidentRepository.findByReportedBy_UserIdAndStatus(currentUser.getUserId(), status,
				pageable);
		return toPagedResponse(page);
	}

	@Override
	public PagedResponse<IncidentResponseDTO> getIncidentsByUserAndCalculatedSeverityPaged(
			IncidentSeverity calculatedSeverity, Pageable pageable) {
		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));
		Page<Incident> page = incidentRepository.findByReportedBy_UserIdAndCalculatedSeverity(
				currentUser.getUserId(), calculatedSeverity, pageable);
		return toPagedResponse(page);
	}

	@Override
	public PagedResponse<IncidentResponseDTO> getIncidentsByUserAndUserMarkedCriticalPaged(
			Boolean userMarkedCritical, Pageable pageable) {
		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));
		Page<Incident> page = incidentRepository.findByReportedBy_UserIdAndIsCritical(
				currentUser.getUserId(), userMarkedCritical, pageable);
		return toPagedResponse(page);
	}

	@Override
	public PagedResponse<IncidentResponseDTO> getAllIncidentsPaged(Pageable pageable) {
		Page<Incident> page = incidentRepository.findAll(pageable);
		return toPagedResponse(page);
	}

	@Override
	public PagedResponse<IncidentResponseDTO> getAllIncidentsByStatusPaged(IncidentStatus status, Pageable pageable) {
		Page<Incident> page = incidentRepository.findByStatus(status, pageable);
		return toPagedResponse(page);
	}

	private PagedResponse<IncidentResponseDTO> toPagedResponse(Page<Incident> page) {
		return PagedResponse.<IncidentResponseDTO>builder()
				.content(page.getContent().stream().map(this::mapToResponseDTO).toList())
				.page(page.getNumber())
				.size(page.getSize())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.last(page.isLast())
				.first(page.isFirst())
				.build();
	}

	@Override
	@Transactional
	public IncidentResponseDTO updateIncidentStatus(Long incidentId, IncidentStatusUpdateRequestDTO request) {
		// If the incident is breached earlier, a row exists in incident_sla_breach.
		// When status becomes RESOLVED/CLOSED, we update that row →
		// BreachStatus.RESOLVED.
		User currentUser = securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));

		// (Optional) Only Manager/Admin should update - you can enforce RBAC here
		// if (currentUser.getRole() == UserRole.EMPLOYEE) throw new
		// AccessDeniedException("Not allowed");

		Incident incident = incidentRepository.findById(incidentId)
				.orElseThrow(() -> new RuntimeException("Incident not found"));

		IncidentStatus oldStatus = incident.getStatus();
		incident.setStatus(request.getStatus());

		Incident saved = incidentRepository.save(incident);
		if (saved.getStatus() == IncidentStatus.RESOLVED) {
			notificationService.notifyReporterIncidentResolved(incident);
		}

		// ✅ Audit log
		auditService.log(saved, currentUser, ActionType.INCIDENT_STATUS_CHANGED,
				"Status changed: " + oldStatus + " -> " + request.getStatus()
						+ (request.getNote() != null ? (" | Note: " + request.getNote()) : ""));

		// ✅ Auto-close breach when resolved/closed
		if (request.getStatus() == IncidentStatus.RESOLVED || request.getStatus() == IncidentStatus.CANCELLED) {

			breachRepository.findByIncident_IncidentId(incidentId).ifPresent(breach -> {
				breach.setBreachStatus(BreachStatus.RESOLVED);
				breachRepository.save(breach);

				auditService.log(saved, currentUser, ActionType.INCIDENT_UPDATED,
						"Breach closed because incident moved to " + request.getStatus());
			});
		}

		return mapToResponseDTO(saved);
	}

	private IncidentResponseDTO mapToResponseDTO(Incident incident) {
		IncidentResponseDTO dto = new IncidentResponseDTO();
		dto.setIncidentId(incident.getIncidentId());
		dto.setDescription(incident.getDescription());
		dto.setStatus(incident.getStatus());
		dto.setCalculatedSeverity(incident.getCalculatedSeverity());
		dto.setReportedDate(incident.getReportedDate());
		dto.setIsCritical(incident.getIsCritical());
		dto.setUserId(incident.getReportedBy().getUserId());
		dto.setUsername(incident.getReportedBy().getUsername());
		dto.setCategoryId(incident.getCategory().getCategoryId());
		dto.setCategoryName(incident.getCategory().getCategoryName());
		dto.setSubCategory(incident.getCategory().getSubCategory());
		dto.setSlaHours(incident.getCategory().getSlaTimeHours());

		return dto;

	}
}