package com.incidenttracker.backend.notification.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.common.security.SecurityService;

import com.incidenttracker.backend.common.enums.NotificationStatus;
import com.incidenttracker.backend.common.enums.NotificationType;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.notification.dto.NotificationRequestDto;
import com.incidenttracker.backend.notification.dto.NotificationResponseDto;
import com.incidenttracker.backend.notification.entity.Notification;
import com.incidenttracker.backend.notification.repository.NotificationRepository;
import com.incidenttracker.backend.task.entity.Task;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final SecurityService securityService;
	private final UserRepository userRepository;

	/**
	 * emitters: Stores active user connections in memory.
	 * ConcurrentHashMap: Thread-safe map to prevent crashes when multiple users
	 * connect.
	 * List<SseEmitter>: We use a List so if a user has 3 tabs open, all 3 get the
	 * notification.
	 */
	private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

	/**
	 * subscribe: Called when frontend opens SSE connection. Creates a long-lived
	 * HTTP connection and registers cleanup on close/timeout.
	 */
	public SseEmitter subscribe() {

		// Fast path: read userId from JWT claim — no DB call
		Long tokenUserId = securityService.getCurrentUserIdFromToken();
		final Long userId = (tokenUserId != null) ? tokenUserId
				: securityService.getCurrentUser()
						.orElseThrow(() -> new RuntimeException("Authentication required"))
						.getUserId();

		// Limit to 1 active SSE connection per user to prevent connection storms
		List<SseEmitter> existingEmitters = emitters.get(userId);
		if (existingEmitters != null && existingEmitters.size() >= 3) {
			log.warn("User {} already has {} SSE connections. Closing oldest to prevent pool exhaustion.",
					userId, existingEmitters.size());
			SseEmitter oldest = existingEmitters.get(0);
			oldest.complete();
			existingEmitters.remove(oldest);
		}

		// SseEmitter(timeout): 5 mins. Short enough to free threads, frontend
		// reconnects.
		SseEmitter emitter = new SseEmitter(300000L);

		// Registers the new emitter into our storage
		emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

		// Backend listens to: onCompletion, onTimeout, onError And removes emitter
		// safely
		emitter.onCompletion(() -> {
			log.info("SSE connection completed for user ID: {}", userId);
			removeEmitter(userId, emitter);
		});
		emitter.onTimeout(() -> {
			log.warn("SSE connection timed out for user ID: {}", userId);
			removeEmitter(userId, emitter);
		});
		emitter.onError((ex) -> {
			log.error("SSE connection error for user ID {}: {}", userId, ex.getMessage());
			removeEmitter(userId, emitter);
		});

		log.info("User {} connected to SSE stream. Total active connections for user: {}",
				userId, emitters.get(userId).size());
		return emitter;
	}

	// removeEmitter: Safely removes a specific connection from the list.
	private void removeEmitter(Long userId, SseEmitter emitter) {
		List<SseEmitter> userEmitters = emitters.get(userId);
		if (userEmitters != null) {
			userEmitters.remove(emitter);
			if (userEmitters.isEmpty()) {
				emitters.remove(userId);
				log.debug("All SSE emitters removed for user ID: {}", userId);
			}
		}
	}

	/**
	 * sendLiveNotification: Pushes notification instantly to browser. No REST call
	 * needed from frontend! It "speaks" directly to the open tab.
	 */
	private void sendLiveNotification(Long userId, NotificationResponseDto payload) {
		List<SseEmitter> userEmitters = emitters.get(userId);
		if (userEmitters == null || userEmitters.isEmpty()) {
			log.debug("No active SSE connections for user ID: {}. Live push skipped.", userId);
			return;
		}

		log.debug("Attempting to send live notification to {} active tabs for user ID: {}",
				userEmitters.size(), userId);

		// Collect dead emitters separately — never modify a list while iterating it
		List<SseEmitter> deadEmitters = new ArrayList<>();

		for (SseEmitter emitter : userEmitters) {
			try {
				emitter.send(SseEmitter.event().name("notification").data(payload));
			} catch (Exception e) {
				// Catches IOException, IllegalStateException ("ResponseBodyEmitter is already
				// set complete")
				// and the Spring-internal "Failed to send" wrapper — all indicate a dead
				// connection
				log.warn("Failed to send SSE to user ID: {} — marking emitter as dead. Reason: {}", userId,
						e.getMessage());
				deadEmitters.add(emitter);
			}
		}

		// Remove all dead emitters after iteration
		for (SseEmitter dead : deadEmitters) {
			removeEmitter(userId, dead);
		}
	}

	/**
	 * createNotification: To be used by other modules.
	 * It saves to the DB and THEN triggers the SSE Push.
	 */
	public NotificationResponseDto createNotification(NotificationRequestDto requestDto) {
		// Save to Database
		Notification n = new Notification();
		User userShell = new User();
		userShell.setUserId(requestDto.getUserId());
		n.setUser(userShell);
		n.setType(requestDto.getType());
		n.setMessage(requestDto.getMessage());

		Notification saved = notificationRepository.save(n);
		log.info("Notification persisted to database with ID: {} for user: {}",
				saved.getNotificationId(), requestDto.getUserId());

		NotificationResponseDto response = mapToResponse(saved);

		/**
		 * If user is offline: No emitter found, push is skipped.
		 * Notification still saved in DB, user fetches later
		 */
		sendLiveNotification(requestDto.getUserId(), response);

		return response;
	}

	// Helper method to convert Entity to ResponseDto
	private NotificationResponseDto mapToResponse(Notification n) {
		return NotificationResponseDto.builder()
				.notificationId(n.getNotificationId())
				.userId(n.getUser().getUserId())
				.type(n.getType())
				.message(n.getMessage())
				.status(n.getStatus())
				.createdDateTime(n.getCreatedDateTime())
				.build();
	}

	// To get all the notifications
	public List<NotificationResponseDto> getAllNotifications() {
		Long userId = securityService.getCurrentUserIdFromToken();
		if (userId == null) {
			userId = securityService.getCurrentUser()
					.orElseThrow(() -> new RuntimeException("Authentication required")).getUserId();
		}
		List<NotificationResponseDto> notifications = notificationRepository
				.findByUser_UserIdOrderByCreatedDateTimeDesc(userId)
				.stream().map(this::mapToResponse).toList();
		log.info("Retrieved {} total notifications for user ID: {}", notifications.size(), userId);
		return notifications;
	}

	// Paginated: get all notifications for current user
	public PagedResponse<NotificationResponseDto> getAllNotificationsPaged(Pageable pageable) {
		Long userId = securityService.getCurrentUserIdFromToken();
		if (userId == null) {
			userId = securityService.getCurrentUser()
					.orElseThrow(() -> new RuntimeException("Authentication required")).getUserId();
		}
		Page<Notification> page = notificationRepository.findByUser_UserId(userId, pageable);
		log.info("Retrieved paged notifications for user ID: {}", userId);
		return toPagedResponse(page);
	}

	// To view all the unread notifications only
	public List<NotificationResponseDto> getUnreads() {
		Long userId = securityService.getCurrentUserIdFromToken();
		if (userId == null) {
			userId = securityService.getCurrentUser()
					.orElseThrow(() -> new RuntimeException("Authentication required")).getUserId();
		}
		List<NotificationResponseDto> unreads = notificationRepository.findUnreadNotifications(userId)
				.stream().map(this::mapToResponse).toList();
		log.info("Retrieved {} unread notifications for user ID: {}", unreads.size(), userId);
		return unreads;
	}

	// Paginated: get unread notifications for current user
	public PagedResponse<NotificationResponseDto> getUnreadsPaged(Pageable pageable) {
		Long userId = securityService.getCurrentUserIdFromToken();
		if (userId == null) {
			userId = securityService.getCurrentUser()
					.orElseThrow(() -> new RuntimeException("Authentication required")).getUserId();
		}
		Page<Notification> page = notificationRepository.findUnreadNotificationsPaged(userId, pageable);
		log.info("Retrieved paged unread notifications for user ID: {}", userId);
		return toPagedResponse(page);
	}

	private PagedResponse<NotificationResponseDto> toPagedResponse(Page<Notification> page) {
		return PagedResponse.<NotificationResponseDto>builder()
				.content(page.getContent().stream().map(this::mapToResponse).toList())
				.page(page.getNumber())
				.size(page.getSize())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.last(page.isLast())
				.first(page.isFirst())
				.build();
	}

	// To mark a notifications as read
	@Transactional
	public void markAsRead(Long notificationId) {
		notificationRepository.findById(notificationId).ifPresentOrElse(n -> {
			n.setStatus(NotificationStatus.READ);
			notificationRepository.save(n);
			log.info("Notification ID: {} marked as READ", notificationId);
		}, () -> log.warn("Notification ID: {} not found for marking as read", notificationId));
	}

	// To mark all notifications as read
	@Transactional
	public void markAllAsRead() {
		Long userId = securityService.getCurrentUserIdFromToken();
		if (userId == null) {
			userId = securityService.getCurrentUser()
					.orElseThrow(() -> new RuntimeException("Authentication required")).getUserId();
		}
		notificationRepository.markAllAsReadForUser(userId);
		log.info("All notifications for user ID: {} marked as READ", userId);
	}

	// INCIDENT_REPORTED: Notify all managers of the incident's department.
	public void notifyAllManager(Incident incident) {
		if (incident == null || incident.getCategory() == null || incident.getCategory().getDepartment() == null) {
			log.warn("Cannot notify managers: incident/category/department is missing");
			return;
		}

		Long departmentId = incident.getCategory().getDepartment().getDepartmentId();
		List<User> managers = userRepository.findByRoleAndDepartment_DepartmentId(UserRole.MANAGER, departmentId);
		if (managers.isEmpty()) {
			log.info("No managers found for department ID: {}", departmentId);
			return;
		}

		String severity = incident.getCalculatedSeverity() != null
				? incident.getCalculatedSeverity().name()
				: "UNKNOWN";
		String message = "Incident reported #" + incident.getIncidentId() + " with severity " + severity;

		for (User manager : managers) {
			NotificationRequestDto request = NotificationRequestDto.builder()
					.userId(manager.getUserId())
					.type(NotificationType.INCIDENT_REPORTED)
					.message(message)
					.build();
			createNotification(request);
		}
	}

	private void notifyAllManagersByDepartment(Long departmentId, NotificationType type, String message) {
		if (departmentId == null) {
			log.warn("Cannot notify managers: departmentId is missing");
			return;
		}

		List<User> managers = userRepository.findByRoleAndDepartment_DepartmentId(UserRole.MANAGER, departmentId);
		if (managers.isEmpty()) {
			log.info("No managers found for department ID: {}", departmentId);
			return;
		}

		for (User manager : managers) {
			NotificationRequestDto request = NotificationRequestDto.builder()
					.userId(manager.getUserId())
					.type(type)
					.message(message)
					.build();
			createNotification(request);
		}
	}

	// INCIDENT_RESOLVED: Notify the employee who reported the incident.
	public void notifyReporterIncidentResolved(Incident incident) {
		if (incident == null || incident.getReportedBy() == null) {
			log.warn("Cannot notify reporter: incident/reportedBy is missing");
			return;
		}

		Long userId = incident.getReportedBy().getUserId();
		String message = "Your incident #" + incident.getIncidentId() + " has been resolved";

		NotificationRequestDto request = NotificationRequestDto.builder()
				.userId(userId)
				.type(NotificationType.INCIDENT_RESOLVED)
				.message(message)
				.build();
		createNotification(request);
	}

	// SLA_BREACHED: Notify assigned employee and the incident's department
	// managers.
	public void notifySlaBreached(Incident incident) {
		if (incident == null) {
			log.warn("Cannot notify SLA breach: incident is missing");
			return;
		}

		String message = "SLA breached for incident #" + incident.getIncidentId();

		if (incident.getCategory() != null && incident.getCategory().getDepartment() != null) {
			Long departmentId = incident.getCategory().getDepartment().getDepartmentId();
			notifyAllManagersByDepartment(departmentId, NotificationType.SLA_BREACHED, message);
		} else {
			log.warn("Cannot notify managers for SLA breach: incident/category/department is missing");
		}
	}

	// URGENT or CANCELLED: Notify managers of the incident's department.
	public void notifyManagersUrgentOrCancelled(Incident incident) {
		if (incident == null || incident.getCategory() == null || incident.getCategory().getDepartment() == null) {
			log.warn("Cannot notify managers: incident/category/department is missing");
			return;
		}

		boolean isUrgent = Boolean.TRUE.equals(incident.getUrgent());
		boolean isCancelled = incident.getStatus() == IncidentStatus.CANCELLED;

		if (!isUrgent && !isCancelled) {
			return;
		}

		Long departmentId = incident.getCategory().getDepartment().getDepartmentId();
		if (isUrgent) {
			String message = "Urgent incident reported: #" + incident.getIncidentId();
			notifyAllManagersByDepartment(departmentId, NotificationType.CRITICAL_INCIDENT_ALERT, message);
		}

		if (isCancelled) {
			String message = "Incident withdrawn (cancelled): #" + incident.getIncidentId();
			notifyAllManagersByDepartment(departmentId, NotificationType.INCIDENT_WITHDRAWN, message);
		}
	}

	// TASK_ASSIGNED: Notify assigned employee when a task is assigned.
	public void notifyEmployee(Task task) {
		if (task == null || task.getAssignedTo() == null) {
			log.warn("Cannot notify employee: task/assignedTo is missing");
			return;
		}

		Long userId = task.getAssignedTo().getUserId();
		String message = "A task #" + task.getTaskId() + " has been assigned to you";

		NotificationRequestDto request = NotificationRequestDto.builder()
				.userId(userId)
				.type(NotificationType.TASK_ASSIGNED)
				.message(message)
				.build();
		createNotification(request);
	}
}
