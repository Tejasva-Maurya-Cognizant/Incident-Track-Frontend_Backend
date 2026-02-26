package com.incidenttracker.backend.notification.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.notification.dto.NotificationRequestDto;
import com.incidenttracker.backend.notification.dto.NotificationResponseDto;
import com.incidenttracker.backend.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe() {
		log.info("Received SSE subscription request for user.");
		return notificationService.subscribe();
	}

	// To create a new notification
	@PostMapping
	public ResponseEntity<NotificationResponseDto> createNotification(@RequestBody NotificationRequestDto requestDto) {
		log.info("Received request to create notification for user ID: {}", requestDto.getUserId());
		NotificationResponseDto response = notificationService.createNotification(requestDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// To display the notifications to the specific user (non-paged)
	@PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
	@GetMapping("/user")
	public ResponseEntity<List<NotificationResponseDto>> getNotifications() {
		log.info("Fetching all notifications for user");
		return ResponseEntity.ok(notificationService.getAllNotifications());
	}

	/**
	 * GET
	 * /api/notifications/user/paged?page=0&size=10&sortBy=createdDateTime&sortDir=desc
	 */
	@PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
	@GetMapping("/user/paged")
	public ResponseEntity<PagedResponse<NotificationResponseDto>> getNotificationsPaged(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdDateTime") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir) {
		log.info("Fetching paged notifications for user");
		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		return ResponseEntity.ok(notificationService.getAllNotificationsPaged(pageable));
	}

	// To display only the unread notifications (non-paged)
	@PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
	@GetMapping("/user/unread")
	public ResponseEntity<List<NotificationResponseDto>> getUnreadNotifications() {
		log.info("Fetching unread notifications for user");
		return ResponseEntity.ok(notificationService.getUnreads());
	}

	/**
	 * GET /api/notifications/user/unread/paged
	 */
	@PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
	@GetMapping("/user/unread/paged")
	public ResponseEntity<PagedResponse<NotificationResponseDto>> getUnreadNotificationsPaged(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdDateTime") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir) {
		log.info("Fetching paged unread notifications for user");
		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		return ResponseEntity.ok(notificationService.getUnreadsPaged(pageable));
	}

	// To change the status of a notification to read
	@PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
	@PatchMapping("/{notificationId}/mark-as-read")
	public ResponseEntity<Void> read(@PathVariable Long notificationId) {
		log.info("Marking notification ID: {} as read", notificationId);
		notificationService.markAsRead(notificationId);
		return ResponseEntity.noContent().build();
	}

	// To mark all notifications as read
	@PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
	@PatchMapping("/user/mark-all-read")
	public ResponseEntity<Void> readAll() {
		log.info("Marking all notifications for user as read");
		notificationService.markAllAsRead();
		return ResponseEntity.noContent().build();
	}
}