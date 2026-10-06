package com.projectexpress.application_service.client;

import com.projectexpress.application_service.dto.CandidateProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "profile-service")
public interface ProfileClient {

    @GetMapping("/api/profiles/{candidateId}")
    CandidateProfileResponse getProfileByCandidateId(
            @PathVariable("candidateId") Long candidateId,
            @RequestHeader("Authorization") String authorizationHeader
    );
}