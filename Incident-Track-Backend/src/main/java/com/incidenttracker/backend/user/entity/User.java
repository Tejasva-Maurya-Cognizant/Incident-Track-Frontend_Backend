package com.incidenttracker.backend.user.entity;

import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.enums.UserStatus;
import com.incidenttracker.backend.department.entity.Department;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id", updatable = false, nullable = false)
	private Long userId;

	@Column(nullable = false, length = 150)
	private String username;

	@Column(nullable = false, length = 254, unique = true) // the RFC 5321 standard technically limits an email address
															// to 254 characters to be compliant with SMTP transport
															// protocols
															// No two users can have the same email
	private String email;

	// @Column(length = 100, name = "department_id")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id")
	private Department department;

	@Column(nullable = false, length = 255)
	private String password; // BCrypt (the standard for Spring Security), the resulting hash is always 60
								// characters long, regardless of how short or long the original password was

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private UserRole role;

	@Column(nullable = false, length = 32)
	@Enumerated(EnumType.STRING)
	private UserStatus status;

	@Override
	public String toString() {
		return "User [UserId=" + userId + ", Name=" + username + ", Email=" + email + ", Department=" + department
				+ ", password=" + password + ", Role=" + role + ", Status=" + status + "]";
	}
}