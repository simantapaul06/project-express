package com.projectexpress.application_service.dto;

import com.projectexpress.application_service.entity.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationResponse(

        Long id,
        Long jobId,
        Long candidateId,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        LocalDateTime updatedAt

) {
}
