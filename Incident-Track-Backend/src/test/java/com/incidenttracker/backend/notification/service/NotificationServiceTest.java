package com.incidenttracker.backend.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.NotificationStatus;
import com.incidenttracker.backend.common.enums.NotificationType;
import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.security.SecurityService;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.notification.dto.NotificationRequestDto;
import com.incidenttracker.backend.notification.dto.NotificationResponseDto;
import com.incidenttracker.backend.notification.entity.Notification;
import com.incidenttracker.backend.notification.repository.NotificationRepository;
import com.incidenttracker.backend.task.entity.Task;
import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.repository.UserRepository;

// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	// Creates a Mockito mock for isolating dependencies.
	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private SecurityService securityService;

	@Mock
	private UserRepository userRepository;

	// Injects mocks into the class under test.
	@InjectMocks
	private NotificationService notificationService;

	private User mockUser;

	// Runs before each test to prepare common setup.
	@BeforeEach
	void setUp() {
		mockUser = new User();
		mockUser.setUserId(1L);
	}

	// Verify that subscribing returns a valid SseEmitter and retrieves user from
	// security context
	@Test
	// Provides a readable name for the test in reports.
	@DisplayName("subscribe() - Should create and return SseEmitter")
	void shouldReturnEmitterWhenUserSubscribes() {
		// Arrange
		when(securityService.getCurrentUserIdFromToken()).thenReturn(1L);

		// Act
		SseEmitter emitter = notificationService.subscribe();

		// Assert
		assertNotNull(emitter);
		assertEquals(300000L, emitter.getTimeout());
		verify(securityService).getCurrentUserIdFromToken();
	}

	// Verify notification creation saves to DB (Request DTO still provides target
	@Test
	@DisplayName("createNotification()")
	void shouldCreateNotificationAndSave() {
		// Arrange
		NotificationRequestDto request = NotificationRequestDto.builder()
				.userId(1L)
				.type(NotificationType.INCIDENT_REPORTED)
				.message("Test Message")
				.build();

		Notification savedNotification = new Notification();
		savedNotification.setNotificationId(100L);
		savedNotification.setUser(mockUser);
		savedNotification.setMessage("Test Message");
		savedNotification.setStatus(NotificationStatus.UNREAD);

		when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

		// Act
		NotificationResponseDto response = notificationService.createNotification(request);

		// Assert
		assertNotNull(response);
		assertEquals(100L, response.getNotificationId());
		verify(notificationRepository, times(1)).save(any(Notification.class));
	}

	@Test
	@DisplayName("getAllNotifications()")
	void shouldReturnAllNotificationsForUser() {
		// Arrange
		Notification n1 = new Notification();
		n1.setUser(mockUser);
		n1.setMessage("Message 1");

		when(securityService.getCurrentUserIdFromToken()).thenReturn(1L);
		when(notificationRepository.findByUser_UserIdOrderByCreatedDateTimeDesc(1L))
				.thenReturn(List.of(n1));

		// Act
		List<NotificationResponseDto> result = notificationService.getAllNotifications();

		// Assert
		assertEquals(1, result.size());
		assertEquals("Message 1", result.get(0).getMessage());
		verify(notificationRepository).findByUser_UserIdOrderByCreatedDateTimeDesc(1L);
	}

	@Test
	@DisplayName("getUnreads()")
	void shouldReturnUnreadNotifications() {
		// Arrange
		Notification n = new Notification();
		n.setUser(mockUser);
		n.setStatus(NotificationStatus.UNREAD);

		when(securityService.getCurrentUserIdFromToken()).thenReturn(1L);
		when(notificationRepository.findUnreadNotifications(1L)).thenReturn(List.of(n));

		// Act
		List<NotificationResponseDto> result = notificationService.getUnreads();

		// Assert
		assertEquals(1, result.size());
		assertEquals(NotificationStatus.UNREAD, result.get(0).getStatus());
		verify(notificationRepository).findUnreadNotifications(1L);
	}

	@Test
	@DisplayName("markAsRead()")
	void shouldMarkSingleNotificationAsRead() {
		// Arrange
		Notification n = new Notification();
		n.setNotificationId(10L);
		n.setStatus(NotificationStatus.UNREAD);

		when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));
		when(notificationRepository.save(any(Notification.class))).thenReturn(n);

		// Act
		notificationService.markAsRead(10L);

		// Assert
		assertEquals(NotificationStatus.READ, n.getStatus());
		verify(notificationRepository).save(n);
	}

	@Test
	@DisplayName("markAllAsRead()")
	void shouldMarkAllNotificationsAsRead() {
		// Arrange
		when(securityService.getCurrentUserIdFromToken()).thenReturn(1L);

		// Act
		notificationService.markAllAsRead();

		// Assert
		verify(notificationRepository, times(1)).markAllAsReadForUser(1L);
	}

	@Test
	@DisplayName("notifyAllManager() - Should notify all managers of department")
	void shouldNotifyAllManagersOfDepartment() {
		// Arrange
		Department dept = new Department();
		dept.setDepartmentId(5L);
		Category cat = new Category();
		cat.setDepartment(dept);
		Incident incident = new Incident();
		incident.setCategory(cat);
		incident.setCalculatedSeverity(IncidentSeverity.HIGH);

		User manager = new User();
		manager.setUserId(2L);

		Notification saved = new Notification();
		saved.setUser(manager);

		when(userRepository.findByRoleAndDepartment_DepartmentId(UserRole.MANAGER, 5L))
				.thenReturn(List.of(manager));
		when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

		// Act
		notificationService.notifyAllManager(incident);

		// Assert
		verify(userRepository).findByRoleAndDepartment_DepartmentId(UserRole.MANAGER, 5L);
		verify(notificationRepository).save(any(Notification.class));
	}

	@Test
	@DisplayName("notifyReporterIncidentResolved()")
	void shouldNotifyReporterWhenIncidentResolved() {
		// Arrange
		User reporter = new User();
		reporter.setUserId(3L);
		Incident incident = new Incident();
		incident.setIncidentId(500L);
		incident.setReportedBy(reporter);

		Notification saved = new Notification();
		saved.setUser(reporter);

		when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

		// Act
		notificationService.notifyReporterIncidentResolved(incident);

		// Assert
		verify(notificationRepository).save(any(Notification.class));
	}

	@Test
	@DisplayName("notifySlaBreached()")
	void shouldNotifyManagersOnSlaBreach() {
		// Arrange
		Department dept = new Department();
		dept.setDepartmentId(10L);
		Category cat = new Category();
		cat.setDepartment(dept);
		Incident incident = new Incident();
		incident.setIncidentId(101L);
		incident.setCategory(cat);

		User manager = new User();
		manager.setUserId(9L);

		Notification saved = new Notification();
		saved.setUser(manager);

		when(userRepository.findByRoleAndDepartment_DepartmentId(UserRole.MANAGER, 10L))
				.thenReturn(List.of(manager));
		when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

		// Act
		notificationService.notifySlaBreached(incident);

		// Assert
		verify(userRepository).findByRoleAndDepartment_DepartmentId(UserRole.MANAGER, 10L);
	}

	@Test
	@DisplayName("notifyManagersUrgentOrCancelled() - Urgent case")
	void shouldNotifyManagersWhenIncidentIsUrgent() {
		// Arrange
		Department dept = new Department();
		dept.setDepartmentId(1L);
		Category cat = new Category();
		cat.setDepartment(dept);
		Incident incident = new Incident();
		incident.setCategory(cat);
		incident.setUrgent(true);

		User manager = new User();
		manager.setUserId(4L);

		Notification saved = new Notification();
		saved.setUser(manager);

		when(userRepository.findByRoleAndDepartment_DepartmentId(UserRole.MANAGER, 1L))
				.thenReturn(List.of(manager));
		when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

		// Act
		notificationService.notifyManagersUrgentOrCancelled(incident);

		// Assert
		verify(notificationRepository).save(any(Notification.class));
	}

	@Test
	@DisplayName("notifyEmployee() - Task Assignment")
	void shouldNotifyEmployeeOnTaskAssignment() {
		// Arrange
		User assignedUser = new User();
		assignedUser.setUserId(7L);
		Task task = new Task();
		task.setTaskId(200L);
		task.setAssignedTo(assignedUser);

		Notification saved = new Notification();
		saved.setUser(assignedUser);

		when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

		// Act
		notificationService.notifyEmployee(task);

		// Assert
		verify(notificationRepository).save(any(Notification.class));
	}
}
