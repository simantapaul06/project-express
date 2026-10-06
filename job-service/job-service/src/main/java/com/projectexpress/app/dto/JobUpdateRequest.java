package com.projectexpress.app.dto;

import com.projectexpress.app.enums.EmploymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobUpdateRequest {// We could reuse JobRequest, but keeping separate DTOs gives us flexibility later.

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Company name is required")
    private String company;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @NotBlank(message = "Experience required is required")
    private String experienceRequired;

    @NotBlank(message = "Salary is required")
    private String salary;

    @NotNull(message = "Location is required")
    private Long locationId;
}