package com.incidenttracker.backend.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDto {

	private String categoryName;

	private String subCategory;

	private Integer slaTimeHours;

	private Boolean isVisible;

	private Long departmentId;

}