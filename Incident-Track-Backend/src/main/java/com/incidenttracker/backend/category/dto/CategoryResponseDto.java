package com.incidenttracker.backend.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDto {
	private Long categoryId;
	private String categoryName;
	private String subCategory;
	private Integer slaTimeHours;
	private Boolean isVisible;
	private String departmentName;
}