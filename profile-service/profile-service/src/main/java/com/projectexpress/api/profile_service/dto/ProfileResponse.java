package com.projectexpress.api.profile_service.dto;

public record ProfileResponse(

        Long id,
        Long candidateId,
        String name,
        String email,
        String phone,
        String profileImageKey,
        String resumeKey
) {
}
