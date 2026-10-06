package com.projectexpress.application_service.dto;

import com.projectexpress.application_service.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(

        @NotNull(message = "Status is required")
        ApplicationStatus status

)
{ }