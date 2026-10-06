package com.projectexpress.application_service.dto;

public record CandidateProfileResponse(
        Long id,
        Long candidateId,
        String name,
        String email,
        String phone,
        String profileImageKey,
        String resumeKey
) {
}