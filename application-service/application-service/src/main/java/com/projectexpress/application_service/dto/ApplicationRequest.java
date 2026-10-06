package com.projectexpress.application_service.dto;

import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
        @NotNull(message = "Job ID is required")
        Long jobId
) { }
