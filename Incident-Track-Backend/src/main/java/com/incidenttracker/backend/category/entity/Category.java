package com.incidenttracker.backend.category.entity;

import com.incidenttracker.backend.department.entity.Department;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "category")
public class Category {
	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
	private Long categoryId;

	private String categoryName;
	private String subCategory;
	private Integer slaTimeHours;
	private Boolean isVisible;

	@ManyToOne
	@JoinColumn(name = "department_id")
	private Department department;

}