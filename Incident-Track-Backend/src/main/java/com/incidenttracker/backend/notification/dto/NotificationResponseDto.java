package com.incidenttracker.backend.notification.dto;

import java.time.LocalDateTime;

import com.incidenttracker.backend.common.enums.NotificationStatus;
import com.incidenttracker.backend.common.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {

	private Long notificationId;

	private Long userId;

	private NotificationType type;

	private String message;

	private NotificationStatus status;

	private LocalDateTime createdDateTime;
}