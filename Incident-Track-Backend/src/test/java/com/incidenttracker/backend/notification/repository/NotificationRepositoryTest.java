package com.incidenttracker.backend.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.incidenttracker.backend.common.enums.NotificationStatus;
import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.enums.UserStatus;
import com.incidenttracker.backend.notification.entity.Notification;
import com.incidenttracker.backend.user.entity.User;

// Slice test: loads only JPA components with an in-memory DB setup.
@DataJpaTest
// Use the "test" Spring profile (loads application-test.properties).
@ActiveProfiles("test")
class NotificationRepositoryTest {

	// Injects a Spring-managed bean into the test.
	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private TestEntityManager entityManager;

	private User testUser;

	// Runs before each test to prepare common setup.
	@BeforeEach
	void setUp() {
		User user = new User();
		user.setUsername("testUser_" + System.currentTimeMillis());
		user.setPassword("securePassword123");
		user.setEmail("test" + System.currentTimeMillis() + "@example.com");
		user.setRole(UserRole.EMPLOYEE);
		user.setStatus(UserStatus.ACTIVE);
		testUser = entityManager.persist(user);
		entityManager.flush();
	}

	// Verify that the repository correctly filters only UNREAD status notifications
	// for a specific user
	@Test
	// Provides a readable name for the test in reports.
	@DisplayName("findUnreadNotifications() - Should filter by UNREAD status after a bulk update")
	void shouldReturnOnlyUnreadNotifications() {
		// Arrange
		Notification n1 = new Notification();
		n1.setUser(testUser);
		n1.setMessage("Message 1 - Will be marked READ");
		entityManager.persist(n1);

		Notification n2 = new Notification();
		n2.setUser(testUser);
		n2.setMessage("Message 2 - Will stay UNREAD");

		entityManager.persist(n2);
		entityManager.flush();

		notificationRepository.markAllAsReadForUser(testUser.getUserId());
		entityManager.clear();

		Notification n3 = new Notification();
		n3.setUser(testUser);
		n3.setMessage("Message 3 - Fresh Unread");

		entityManager.persist(n3);
		entityManager.flush();
		entityManager.clear();

		// Act: Fetch only unread
		List<Notification> unreadList = notificationRepository.findUnreadNotifications(testUser.getUserId());

		// Assert: Size should be 1 (only the fresh n3)
		assertThat(unreadList).hasSize(1);
		assertThat(unreadList.get(0).getMessage()).isEqualTo("Message 3 - Fresh Unread");
		assertThat(unreadList.get(0).getStatus()).isEqualTo(NotificationStatus.UNREAD);
	}

	// Verify that searching for unread notifications returns an empty list when all
	@Test
	@DisplayName("shouldReturnEmptyListWhenNoUnreadExist() - Accommodating @PrePersist")
	void shouldReturnEmptyListWhenNoUnreadExist() {
		// Arrange
		Notification n1 = new Notification();
		n1.setUser(testUser);
		n1.setMessage("About to be read");

		entityManager.persist(n1);
		entityManager.flush();

		// Act: Manually update to READ to override the @PrePersist default
		notificationRepository.markAllAsReadForUser(testUser.getUserId());
		entityManager.clear();

		// Act: Fetch unread
		List<Notification> result = notificationRepository.findUnreadNotifications(testUser.getUserId());

		// Assert
		assertThat(result).isEmpty();
	}

	// Verify the bulk update JPQL query correctly changes status from UNREAD to
	@Test
	@DisplayName("markAllAsReadForUser() - Bulk Update Verification")
	void shouldUpdateStatusToReadForUser() {
		// Arrange
		Notification n1 = new Notification();
		n1.setUser(testUser);
		n1.setMessage("Verify Update");

		entityManager.persist(n1); // @PrePersist makes this UNREAD
		entityManager.flush();

		// Act
		notificationRepository.markAllAsReadForUser(testUser.getUserId());
		entityManager.clear();

		// Assert
		Notification updatedNotification = entityManager.find(Notification.class, n1.getNotificationId());
		assertThat(updatedNotification.getStatus()).isEqualTo(NotificationStatus.READ);
	}
}
