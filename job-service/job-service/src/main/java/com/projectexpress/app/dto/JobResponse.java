package com.projectexpress.app.dto;

import com.projectexpress.app.enums.EmploymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class JobResponse {

    private Long id;

    private String title;

    private String company;

    private String description;

    private EmploymentType employmentType;

    private String experienceRequired;

    private String salary;

    private LocationResponse location;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long createdBy;
}
