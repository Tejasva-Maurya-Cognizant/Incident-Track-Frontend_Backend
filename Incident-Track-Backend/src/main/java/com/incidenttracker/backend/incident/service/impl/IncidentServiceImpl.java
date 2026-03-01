package com.incidenttracker.backend.incident.service.impl;

import java.time.LocalDateTime;
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
import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.security.SecurityService;
import com.incidenttracker.backend.common.util.DateTimeUtils;
import com.incidenttracker.backend.incident.dto.IncidentRequestDTO;
import com.incidenttracker.backend.incident.dto.IncidentResponseDTO;
import com.incidenttracker.backend.incident.dto.IncidentStatusUpdateRequestDTO;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.incident.repository.IncidentRepository;
import com.incidenttracker.backend.incident.service.IncidentService;
import com.incidenttracker.backend.notification.service.NotificationService;
import com.incidenttracker.backend.task.repository.TaskRepository;
import com.incidenttracker.backend.user.entity.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

	private final IncidentRepository incidentRepository;
	private final TaskRepository taskRepository;
	private final CategoryRepository categoryRepository;
	private final SecurityService securityService;
	private final IncidentSlaBreachRepository breachRepository;
	private final AuditService auditService;
	private final NotificationService notificationService;

	private User getCurrentUserRequired() {
		return securityService.getCurrentUser()
				.orElseThrow(() -> new RuntimeException("Authentication required"));
	}

	private Long getDepartmentIdRequired(User user) {
		if (user.getDepartment() == null || user.getDepartment().getDepartmentId() == null) {
			throw new IllegalStateException("Current user is not mapped to a department.");
		}
		return user.getDepartment().getDepartmentId();
	}

	private Incident getPrivilegedAccessibleIncident(Long incidentId, User currentUser) {
		if (currentUser.getRole() == UserRole.MANAGER) {
			return incidentRepository
					.findByIncidentIdAndCategory_Department_DepartmentId(incidentId, getDepartmentIdRequired(currentUser))
					.orElseThrow(() -> new RuntimeException("Incident not found or access denied"));
		}
		return incidentRepository.findById(incidentId)
				.orElseThrow(() -> new RuntimeException("Incident not found"));
	}

	private Incident getTaskAccessibleIncident(Long incidentId, User currentUser) {
		Incident ownIncident = incidentRepository.findByIncidentIdAndReportedBy_UserId(incidentId, currentUser.getUserId())
				.orElse(null);
		if (ownIncident != null) {
			return ownIncident;
		}

		if (currentUser.getRole() == UserRole.EMPLOYEE
				&& taskRepository.existsByIncident_IncidentIdAndAssignedTo_UserId(incidentId, currentUser.getUserId())) {
			return incidentRepository.findById(incidentId)
					.orElseThrow(() -> new RuntimeException("Incident not found"));
		}

		throw new RuntimeException("Incident not found or access denied");
	}

	private IncidentSeverity calculateSeverity(Boolean urgent, Integer slaHours) {
		if (Boolean.TRUE.equals(urgent)) {
			return IncidentSeverity.CRITICAL;
		}
		if (slaHours == null) {
			return IncidentSeverity.LOW;
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

	private LocalDateTime calculateSlaDueAt(Category category, LocalDateTime reportedAt, Boolean urgent) {
		if (category == null || category.getSlaTimeHours() == null) {
			return null;
		}

		long baseMinutes = category.getSlaTimeHours() * 60L;
		if (baseMinutes <= 0) {
			return reportedAt;
		}

		long effectiveMinutes = Boolean.TRUE.equals(urgent) ? Math.max(1L, baseMinutes / 2L) : baseMinutes;
		return DateTimeUtils.truncateToSeconds(reportedAt.plusMinutes(effectiveMinutes));
	}

	@Override
	@Transactional
	public IncidentResponseDTO createIncident(IncidentRequestDTO dto) {

		Category category = categoryRepository.findById(dto.getCategoryId())
				.orElseThrow(() -> new RuntimeException("Category not found"));

		User currentUser = getCurrentUserRequired();

		Incident incident = new Incident();
		incident.setDescription(dto.getDescription());
		incident.setReportedBy(currentUser);
		incident.setCategory(category);
		incident.setUrgent(Boolean.TRUE.equals(dto.getUrgent()));
		LocalDateTime reportedAt = DateTimeUtils.nowTruncatedToSeconds();
		incident.setReportedDate(reportedAt);
		incident.setSlaDueAt(calculateSlaDueAt(category, reportedAt, incident.getUrgent()));
		IncidentSeverity severity = calculateSeverity(incident.getUrgent(), category.getSlaTimeHours());
		incident.setCalculatedSeverity(severity);

		Incident saved = incidentRepository.save(incident);

		auditService.log(saved, currentUser, ActionType.INCIDENT_CREATED, "Incident created with incident Id :- "
				+ saved.getIncidentId() + "of category :- " + saved.getCategory().getCategoryName());
		notificationService.notifyAllManager(saved);
		notificationService.notifyManagersUrgentOrCancelled(saved);
		return mapToResponseDTO(saved);
	}

	@Override
	public List<IncidentResponseDTO> getIncidentsUser() {
		User currentUser = getCurrentUserRequired();

		return incidentRepository.findByReportedBy_UserId(currentUser.getUserId())
				.stream()
				.map(this::mapToResponseDTO)
				.toList();

	}

	@Override
	public List<IncidentResponseDTO> getIncidentsByUserAndStatus(IncidentStatus status) {
		User currentUser = getCurrentUserRequired();
		return incidentRepository.findByReportedBy_UserIdAndStatus(currentUser.getUserId(), status)
				.stream()
				.map(this::mapToResponseDTO)
				.toList();
	}

	@Override
	public List<IncidentResponseDTO> getIncidentsByUserAndCalculatedSeverity(
			IncidentSeverity calculatedSeverity) {
		User currentUser = getCurrentUserRequired();
		return incidentRepository
				.findByReportedBy_UserIdAndCalculatedSeverity(currentUser.getUserId(), calculatedSeverity)
				.stream()
				.map(this::mapToResponseDTO)
				.toList();
	}

	@Override
	public List<IncidentResponseDTO> getIncidentsByUserAndUrgent(Boolean urgent) {
		User currentUser = getCurrentUserRequired();

		return incidentRepository.findByReportedBy_UserIdAndUrgent(currentUser.getUserId(), urgent)
				.stream()
				.map(this::mapToResponseDTO)
				.toList();
	}

	@Override
	public IncidentResponseDTO getIncidentDetails(Long incidentId) {
		User currentUser = getCurrentUserRequired();
		Incident incident = incidentRepository.findByIncidentIdAndReportedBy_UserId(incidentId, currentUser.getUserId())
				.orElseThrow(() -> new RuntimeException("Incident not found or access denied"));
		return mapToResponseDTO(incident);

	}

	@Override
	public IncidentResponseDTO getIncidentDetailsForTaskContext(Long incidentId) {
		User currentUser = getCurrentUserRequired();
		Incident incident = currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.MANAGER
				? getPrivilegedAccessibleIncident(incidentId, currentUser)
				: getTaskAccessibleIncident(incidentId, currentUser);
		return mapToResponseDTO(incident);
	}

	@Override
	public IncidentResponseDTO getIncidentDetailsForAdmin(Long incidentId) {
		User currentUser = getCurrentUserRequired();
		Incident incident = getPrivilegedAccessibleIncident(incidentId, currentUser);
		return mapToResponseDTO(incident);
	}

	@Override
	public IncidentResponseDTO withdrawIncident(Long incidentId) {
		User currentUser = getCurrentUserRequired();
		Incident incident = incidentRepository.findByIncidentIdAndReportedBy_UserId(incidentId, currentUser.getUserId())
				.orElseThrow(() -> new RuntimeException("Incident not found or access denied"));

		System.out.println(currentUser.getUsername());
		if (!currentUser.getUserId().equals(incident.getReportedBy().getUserId())) {
			throw new AccessDeniedException("You are not authorized to cancel this incident.");
		}

		if (incident.getStatus() == IncidentStatus.RESOLVED
				|| incident.getStatus() == IncidentStatus.CANCELLED
				|| incident.getStatus() == IncidentStatus.IN_PROGRESS) {
			throw new IllegalStateException("Cannot cancel an incident that is already " + incident.getStatus());
		}

		incident.setStatus(IncidentStatus.CANCELLED);
		incident.setResolvedDate(DateTimeUtils.nowTruncatedToSeconds());
		Incident saved = incidentRepository.save(incident);

		notificationService.notifyManagersUrgentOrCancelled(incident);

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
		User currentUser = getCurrentUserRequired();
		List<Incident> incidents = currentUser.getRole() == UserRole.MANAGER
				? incidentRepository.findByCategory_Department_DepartmentId(getDepartmentIdRequired(currentUser))
				: incidentRepository.findAll();
		return incidents.stream().map(this::mapToResponseDTO).toList();
	}

	// ---- Paginated implementations ----

	@Override
	public PagedResponse<IncidentResponseDTO> getIncidentsUserPaged(Pageable pageable) {
		User currentUser = getCurrentUserRequired();
		Page<Incident> page = incidentRepository.findByReportedBy_UserId(currentUser.getUserId(), pageable);
		return toPagedResponse(page);
	}

	@Override
	public PagedResponse<IncidentResponseDTO> getIncidentsByUserAndStatusPaged(IncidentStatus status,
			Pageable pageable) {
		User currentUser = getCurrentUserRequired();
		Page<Incident> page = incidentRepository.findByReportedBy_UserIdAndStatus(currentUser.getUserId(), status,
				pageable);
		return toPagedResponse(page);
	}

	@Override
	public PagedResponse<IncidentResponseDTO> getIncidentsByUserAndCalculatedSeverityPaged(
			IncidentSeverity calculatedSeverity, Pageable pageable) {
		User currentUser = getCurrentUserRequired();
		Page<Incident> page = incidentRepository.findByReportedBy_UserIdAndCalculatedSeverity(
				currentUser.getUserId(), calculatedSeverity, pageable);
		return toPagedResponse(page);
	}

	@Override
	public PagedResponse<IncidentResponseDTO> getIncidentsByUserAndUrgentPaged(
			Boolean urgent, Pageable pageable) {
		User currentUser = getCurrentUserRequired();
		Page<Incident> page = incidentRepository.findByReportedBy_UserIdAndUrgent(
				currentUser.getUserId(), urgent, pageable);
		return toPagedResponse(page);
	}

	@Override
	public PagedResponse<IncidentResponseDTO> getAllIncidentsPaged(Pageable pageable) {
		User currentUser = getCurrentUserRequired();
		Page<Incident> page = currentUser.getRole() == UserRole.MANAGER
				? incidentRepository.findByCategory_Department_DepartmentId(getDepartmentIdRequired(currentUser), pageable)
				: incidentRepository.findAll(pageable);
		return toPagedResponse(page);
	}

	@Override
	public PagedResponse<IncidentResponseDTO> getAllIncidentsByStatusPaged(IncidentStatus status, Pageable pageable) {
		User currentUser = getCurrentUserRequired();
		Page<Incident> page = currentUser.getRole() == UserRole.MANAGER
				? incidentRepository.findByStatusAndCategory_Department_DepartmentId(
						status, getDepartmentIdRequired(currentUser), pageable)
				: incidentRepository.findByStatus(status, pageable);
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
		User currentUser = getCurrentUserRequired();
		Incident incident = getPrivilegedAccessibleIncident(incidentId, currentUser);

		if (request == null || request.getStatus() == null) {
			throw new IllegalStateException("A valid incident status is required.");
		}

		if (incident.getStatus() == IncidentStatus.RESOLVED || incident.getStatus() == IncidentStatus.CANCELLED) {
			throw new IllegalStateException("Closed incidents cannot change status.");
		}

		if (incident.getStatus() == IncidentStatus.IN_PROGRESS) {
			throw new IllegalStateException("Incidents with an active task must be closed through task completion.");
		}

		if (request.getStatus() == IncidentStatus.OPEN || request.getStatus() == IncidentStatus.IN_PROGRESS) {
			throw new IllegalStateException("Incident status can only be changed manually to RESOLVED or CANCELLED.");
		}

		if (incident.getStatus() == request.getStatus()) {
			throw new IllegalStateException("Incident is already " + request.getStatus() + ".");
		}

		IncidentStatus oldStatus = incident.getStatus();
		incident.setStatus(request.getStatus());
		incident.setResolvedDate(DateTimeUtils.nowTruncatedToSeconds());

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
		dto.setResolvedDate(incident.getResolvedDate());
		dto.setUrgent(incident.getUrgent());
		dto.setSlaDueAt(incident.getSlaDueAt());
		dto.setUserId(incident.getReportedBy().getUserId());
		dto.setUsername(incident.getReportedBy().getUsername());
		dto.setCategoryId(incident.getCategory().getCategoryId());
		dto.setCategoryName(incident.getCategory().getCategoryName());
		dto.setSubCategory(incident.getCategory().getSubCategory());
		dto.setDepartmentName(
				incident.getCategory().getDepartment() != null ? incident.getCategory().getDepartment().getDepartmentName() : null);
		dto.setSlaHours(incident.getCategory().getSlaTimeHours());

		return dto;

	}
}
