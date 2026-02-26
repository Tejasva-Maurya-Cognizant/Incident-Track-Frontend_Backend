package com.incidenttracker.backend.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	public Optional<User> findByEmail(String email);

	public Optional<User> findByUsername(String username);

	public List<User> findByRole(UserRole role);

	public List<User> findByDepartment_DepartmentId(Long departmentId);

	public List<User> findByRoleAndDepartment_DepartmentId(UserRole role, Long departmentId);

	// ---- Pageable versions ----
	public Page<User> findAll(Pageable pageable);

	public Page<User> findByRole(UserRole role, Pageable pageable);

	public Page<User> findByDepartment_DepartmentId(Long departmentId, Pageable pageable);

	public Page<User> findByRoleAndDepartment_DepartmentId(UserRole role, Long departmentId, Pageable pageable);
}
