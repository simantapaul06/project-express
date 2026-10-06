package com.projectexpress.application_service.controller;

import com.projectexpress.application_service.dto.*;
import com.projectexpress.application_service.entity.JobApplication;
import com.projectexpress.application_service.mapper.JobApplicationMapper;
import com.projectexpress.application_service.security.AuthenticatedUser;
import com.projectexpress.application_service.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final JobApplicationMapper jobApplicationMapper;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody ApplicationRequest request, Authentication authentication,
                                                     @RequestHeader("Authorization") String authorizationHeader) {

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

        JobApplication application =
                jobApplicationService.apply(
                        request,
                        authenticatedUser.getUserId(),
                        authorizationHeader
                );

        return ResponseEntity.ok(
                jobApplicationMapper.toResponse(application)
        );
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<JobApplication>> getMyApplications(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        List<JobApplication> applications = jobApplicationService.getMyApplications(user.getUserId());
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobApplication>> getApplicationsByJobId(@PathVariable Long jobId) {
        List<JobApplication> applications = jobApplicationService.getApplicationsByJobId(jobId);
        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobApplication> updateApplicationStatus(@PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {

        JobApplication application = jobApplicationService.updateApplicationStatus(applicationId, request.status());
        return ResponseEntity.ok(application);
    }



    @PatchMapping("/{applicationId}/withdraw")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<JobApplication> withdrawApplication(@PathVariable Long applicationId, Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        JobApplication application = jobApplicationService.withdrawApplication(applicationId, user.getUserId());
        return ResponseEntity.ok(application);
    }

    @GetMapping("/{applicationId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<JobApplication> getApplicationById(@PathVariable Long applicationId, Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        JobApplication application = jobApplicationService.getApplicationById(applicationId, user.getUserId(), role);
        return ResponseEntity.ok(application);
    }


    @GetMapping("/{applicationId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationDetailsResponse> getApplicationDetails(@PathVariable Long applicationId, @RequestHeader("Authorization") String authorizationHeader) {

        ApplicationDetailsResponse response = jobApplicationService.getApplicationDetails(applicationId, authorizationHeader);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/job/{jobId}/applicants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobApplicantResponse>> getApplicantsByJobId(@PathVariable Long jobId, @RequestHeader("Authorization") String authorizationHeader) {

        List<JobApplicantResponse> response = jobApplicationService.getApplicantsByJobId(jobId, authorizationHeader);

        return ResponseEntity.ok(response);
    }

}