package com.incidenttracker.backend.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class DepartmentResponseDto {

    // Department identifier returned to clients.
    @NotNull
    private Long departmentId;

    // Department display name returned to clients.
    @NotBlank
    private String departmentName;

}
