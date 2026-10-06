package com.projectexpress.application_service.dto;

import com.projectexpress.application_service.entity.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationDetailsResponse(
        Long applicationId,
        Long jobId,
        Long candidateId,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        LocalDateTime updatedAt,
        CandidateProfileResponse candidate
) {
}