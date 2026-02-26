package com.incidenttracker.backend.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidenttracker.backend.notification.dto.NotificationRequestDto;
import com.incidenttracker.backend.notification.dto.NotificationResponseDto;
import com.incidenttracker.backend.notification.service.NotificationService;
import com.incidenttracker.backend.user.service.CustomUserDetailsService;
import com.incidenttracker.backend.user.config.JWTUtil;

@WebMvcTest(value = NotificationController.class, excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(ObjectMapper.class)
class NotificationControllerTest {

	// Injects a Spring-managed bean into the test.
	@Autowired
	private MockMvc mockMvc;

	// Creates a Mockito mock for isolating dependencies.
	@MockitoBean
	private NotificationService notificationService;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private JWTUtil jwtUtil;

	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;

	// Verify SSE subscription endpoint triggers asynchronous dispatch
	@Test
	// Provides a readable name for the test in reports.
	@DisplayName("GET /subscribe - Should initiate SSE")
	void shouldSubscribeToSse() throws Exception {
		// Arrange
		when(notificationService.subscribe()).thenReturn(new SseEmitter());

		// Act & Assert
		mockMvc.perform(get("/api/notifications/subscribe"))
				.andExpect(status().isOk())
				.andReturn().getRequest().isAsyncStarted();
	}

	@Test
	@DisplayName("POST /api/notifications - Should return 201 Created")
	void shouldCreateNotification() throws Exception {
		// Arrange
		NotificationRequestDto request = new NotificationRequestDto();
		request.setUserId(1L);
		request.setMessage("Alert Message");

		NotificationResponseDto response = NotificationResponseDto.builder()
				.notificationId(1L)
				.message("Alert Message")
				.build();

		when(notificationService.createNotification(any(NotificationRequestDto.class))).thenReturn(response);

		// Act & Assert
		mockMvc.perform(post("/api/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.notificationId").value(1))
				.andExpect(jsonPath("$.message").value("Alert Message"));
	}

	@Test
	@DisplayName("GET /user - Should return all notifications for user")
	void shouldGetAllNotifications() throws Exception {
		// Arrange
		NotificationResponseDto n1 = NotificationResponseDto.builder().message("Msg 1").build();
		NotificationResponseDto n2 = NotificationResponseDto.builder().message("Msg 2").build();

		when(notificationService.getAllNotifications()).thenReturn(List.of(n1, n2));

		// Act & Assert
		mockMvc.perform(get("/api/notifications/user"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2))
				.andExpect(jsonPath("$[0].message").value("Msg 1"))
				.andExpect(jsonPath("$[1].message").value("Msg 2"));
	}

	@Test
	@DisplayName("GET /user/unread - Should return unread list")
	void shouldGetUnreadNotifications() throws Exception {
		// Arrange
		NotificationResponseDto response = NotificationResponseDto.builder().message("Unread Item").build();
		when(notificationService.getUnreads()).thenReturn(List.of(response));

		// Act & Assert
		mockMvc.perform(get("/api/notifications/user/unread"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(1))
				.andExpect(jsonPath("$[0].message").value("Unread Item"));
	}

	@Test
	@DisplayName("PATCH /{id}/mark-as-read - Should return 204 No Content")
	void shouldMarkAsRead() throws Exception {
		// Arrange
		Long notificationId = 1L;
		doNothing().when(notificationService).markAsRead(notificationId);

		// Act & Assert
		mockMvc.perform(patch("/api/notifications/" + notificationId + "/mark-as-read"))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("PATCH /user/mark-all-read - Should return 204 No Content")
	void shouldMarkAllAsRead() throws Exception {
		// Arrange
		doNothing().when(notificationService).markAllAsRead();

		// Act & Assert
		mockMvc.perform(patch("/api/notifications/user/mark-all-read"))
				.andExpect(status().isNoContent());
	}
}