package com.incidenttracker.backend.notification.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.incidenttracker.backend.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByUser_UserIdOrderByCreatedDateTimeDesc(Long userId);

	@Query("SELECT n FROM Notification n WHERE n.user.userId=:userId AND n.status='UNREAD' ORDER BY n.createdDateTime DESC")
	List<Notification> findUnreadNotifications(@Param("userId") Long userId);

	@Modifying
	@Transactional
	@Query("UPDATE Notification n SET n.status='READ' WHERE n.user.userId = :userId and n.status='UNREAD'")
	void markAllAsReadForUser(Long userId);

	// ---- Pageable versions ----
	Page<Notification> findByUser_UserId(Long userId, Pageable pageable);

	@Query("SELECT n FROM Notification n WHERE n.user.userId=:userId AND n.status='UNREAD' ORDER BY n.createdDateTime DESC")
	Page<Notification> findUnreadNotificationsPaged(@Param("userId") Long userId, Pageable pageable);
}
