package com.incidenttracker.backend.department.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DepartmentRequestDto {

    // Required input field for creating a department.
    @NotBlank
    private String departmentName;

}
