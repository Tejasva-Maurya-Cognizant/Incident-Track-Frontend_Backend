package com.incidenttracker.backend.notification.entity;

import java.time.LocalDateTime;

import com.incidenttracker.backend.common.enums.NotificationStatus;
import com.incidenttracker.backend.common.enums.NotificationType;
import com.incidenttracker.backend.common.util.DateTimeUtils;
import com.incidenttracker.backend.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Notification {

	@GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
	@Id
	private Long notificationId;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	private NotificationType type;

	private String message;

	@Enumerated(EnumType.STRING)
	private NotificationStatus status;

	private LocalDateTime createdDateTime;

	@PrePersist
	protected void onCreate() {
		if (this.createdDateTime == null) {
			this.createdDateTime = DateTimeUtils.nowTruncatedToSeconds();
		}
		if (this.status == null) {
			this.status = NotificationStatus.UNREAD;
		}
	}

}
