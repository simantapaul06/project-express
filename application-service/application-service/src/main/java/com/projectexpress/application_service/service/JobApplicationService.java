package com.projectexpress.application_service.service;

import com.projectexpress.application_service.dto.ApplicationRequest;
import com.projectexpress.application_service.dto.JobApplicantResponse;
import com.projectexpress.application_service.entity.ApplicationStatus;
import com.projectexpress.application_service.entity.ApplicationStatusHistory;
import com.projectexpress.application_service.entity.JobApplication;
import com.projectexpress.application_service.event.JobApplicationCreatedEvent;
import com.projectexpress.application_service.event.JobApplicationEventProducer;
import com.projectexpress.application_service.exception.ApplicationNotFoundException;
import com.projectexpress.application_service.exception.CandidateProfileNotFoundException;
import com.projectexpress.application_service.exception.DuplicateApplicationException;
import com.projectexpress.application_service.repository.ApplicationStatusHistoryRepository;
import com.projectexpress.application_service.repository.JobApplicationRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.projectexpress.application_service.client.ProfileClient;
import com.projectexpress.application_service.dto.ApplicationDetailsResponse;
import com.projectexpress.application_service.dto.CandidateProfileResponse;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobApplicationEventProducer eventProducer;
    private final JobValidationService jobValidationService;
    private final ProfileClient profileClient;
    private final ApplicationStatusHistoryRepository applicationStatusHistoryRepository;

    public JobApplication apply(ApplicationRequest request, Long candidateId, String authorizationHeader) {

        log.info("Candidate {} is attempting to apply for job {}", candidateId, request.jobId());

        Optional<JobApplication> existingApplication = jobApplicationRepository.findByCandidateIdAndJobId(candidateId, request.jobId());

        log.debug("Validating job existence before application: jobId={}, candidateId={}", request.jobId(), candidateId);
        // Verify that the job actually exists in job-service
        jobValidationService.validateJob(request.jobId(), authorizationHeader);

        // Previous application exists
        if (existingApplication.isPresent()) {

            JobApplication application = existingApplication.get();

            // Previous application was withdrawn -> allow re-application
            if (application.getStatus() == ApplicationStatus.WITHDRAWN) {

                application.setStatus(ApplicationStatus.APPLIED);

                JobApplication savedApplication = jobApplicationRepository.save(application);

                // Save re-application APPLIED status in history
                ApplicationStatusHistory history =
                        ApplicationStatusHistory.builder()
                                .applicationId(savedApplication.getId())
                                .status(ApplicationStatus.APPLIED)
                                .build();

                applicationStatusHistoryRepository.save(history);

                log.info("Candidate {} successfully re-applied for job {}. applicationId={}", candidateId, request.jobId(), savedApplication.getId());
                return savedApplication;
            }
            log.warn("Duplicate application attempt: candidateId={}, jobId={}", candidateId, request.jobId());
            throw new DuplicateApplicationException("You have already applied for this job");
        }

        // First-time application
        JobApplication application = JobApplication.builder()
                .jobId(request.jobId())
                .candidateId(candidateId)
                .status(ApplicationStatus.APPLIED)
                .build();

        JobApplication savedApplication = jobApplicationRepository.save(application);

        // Save initial APPLIED status in history
        ApplicationStatusHistory history = ApplicationStatusHistory.builder()
                        .applicationId(savedApplication.getId())
                        .status(savedApplication.getStatus())
                        .build();

        applicationStatusHistoryRepository.save(history);

        // Create and publish Kafka event
        JobApplicationCreatedEvent event = JobApplicationCreatedEvent.builder()
                        .applicationId(savedApplication.getId())
                        .jobId(savedApplication.getJobId())
                        .candidateId(savedApplication.getCandidateId())
                        .status(savedApplication.getStatus().name())
                        .appliedAt(savedApplication.getAppliedAt())
                        .build();

        log.debug("Publishing JobApplicationCreatedEvent: applicationId={}", savedApplication.getId());
        eventProducer.sendApplicationCreateEvent(event);

        return savedApplication;
    }

    public List<JobApplication> getMyApplications(Long candidateId) {
        return jobApplicationRepository.findByCandidateId(candidateId);
    }


    public List<JobApplication> getApplicationsByJobId(Long jobId) {
        return jobApplicationRepository.findByJobId(jobId);
    }


    public JobApplication updateApplicationStatus(Long applicationId, ApplicationStatus newStatus) {

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + applicationId));

        log.info("Updating application status: applicationId={}, currentStatus={}, newStatus={}", applicationId, application.getStatus(), newStatus);

        ApplicationStatus currentStatus = application.getStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            log.warn("Invalid application status transition: applicationId={}, {} -> {}", applicationId, currentStatus, newStatus);
            throw new IllegalStateException("Invalid application status transition: " + currentStatus + " -> " + newStatus);
        }

        application.setStatus(newStatus);

        JobApplication savedApplication = jobApplicationRepository.save(application);

    // Save status change in history
        ApplicationStatusHistory history =
                ApplicationStatusHistory.builder()
                        .applicationId(savedApplication.getId())
                        .status(savedApplication.getStatus())
                        .build();

        applicationStatusHistoryRepository.save(history);

        log.info("Application status updated successfully: applicationId={}, status={}", savedApplication.getId(), savedApplication.getStatus());
        return savedApplication;
    }

    private boolean isValidStatusTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {

        return switch (currentStatus) {

            case APPLIED ->
                    newStatus == ApplicationStatus.UNDER_REVIEW
                            || newStatus == ApplicationStatus.REJECTED;

            case UNDER_REVIEW ->
                    newStatus == ApplicationStatus.SHORTLISTED
                            || newStatus == ApplicationStatus.REJECTED;

            case SHORTLISTED ->
                    newStatus == ApplicationStatus.INTERVIEW
                            || newStatus == ApplicationStatus.REJECTED;

            case INTERVIEW ->
                    newStatus == ApplicationStatus.SELECTED
                            || newStatus == ApplicationStatus.REJECTED;

            // Final states
            case SELECTED, REJECTED, WITHDRAWN -> false;
        };
    }

    public JobApplication withdrawApplication(Long applicationId, Long candidateId) {

        JobApplication application = jobApplicationRepository
                        .findByIdAndCandidateId(applicationId, candidateId)
                        .orElseThrow(() -> new ApplicationNotFoundException(
                                        "Application not found with id: " + applicationId
                                ));

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            log.warn("Withdrawal rejected because application is already withdrawn: applicationId={}", applicationId);
            throw new IllegalStateException("Application is already withdrawn");
        }

        if (application.getStatus() == ApplicationStatus.SELECTED || application.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Application cannot be withdrawn because its current status is " + application.getStatus());
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);

        JobApplication savedApplication = jobApplicationRepository.save(application);

        ApplicationStatusHistory history =
                ApplicationStatusHistory.builder()
                        .applicationId(savedApplication.getId())
                        .status(ApplicationStatus.WITHDRAWN)
                        .build();

        applicationStatusHistoryRepository.save(history);

        log.info("Application withdrawn successfully: applicationId={}, candidateId={}", savedApplication.getId(), candidateId);
        return savedApplication;
    }

//an ADMIN can view any application, while a USER can view only their own application.
    public JobApplication getApplicationById(Long applicationId, Long candidateId, String role) {

        // ADMIN can access any application
        if ("ROLE_ADMIN".equals(role)) {

            return jobApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + applicationId));
        }

        // USER can access only their own application
        return jobApplicationRepository.findByIdAndCandidateId(applicationId, candidateId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + applicationId));
    }

    public ApplicationDetailsResponse getApplicationDetails(Long applicationId, String authorizationHeader) {

        // 1. Get application from application-service database
        JobApplication application = jobApplicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + applicationId));

        // 2. Get candidate profile from profile-service
        CandidateProfileResponse candidate;

        try {
            candidate = profileClient.getProfileByCandidateId(application.getCandidateId(), authorizationHeader);
        } catch (FeignException.NotFound ex) {
            throw new CandidateProfileNotFoundException("Candidate profile not found for candidateId: " + application.getCandidateId());
        }

        // 3. Combine both responses
        return new ApplicationDetailsResponse(
                application.getId(),
                application.getJobId(),
                application.getCandidateId(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getUpdatedAt(),
                candidate
        );
    }

    public List<JobApplicantResponse> getApplicantsByJobId(Long jobId, String authorizationHeader) {

        // 1. Get all applications for this job
        List<JobApplication> applications = jobApplicationRepository.findByJobId(jobId);

        // 2. For each application, get candidate profile
        return applications.stream()
                .map(application -> {

                    CandidateProfileResponse candidate;

                    try {
                        candidate = profileClient.getProfileByCandidateId(application.getCandidateId(), authorizationHeader);
                    } catch (FeignException.NotFound ex) {
                        // Old application may belong to a candidate
                        // created before automatic profile creation existed.
                        candidate = null;
                    }

                    // 3. Combine application + candidate profile
                    return new JobApplicantResponse(
                            application.getId(),
                            application.getJobId(),
                            application.getCandidateId(),
                            application.getStatus(),
                            application.getAppliedAt(),
                            application.getUpdatedAt(),
                            candidate
                    );
                })
                .toList();
    }
}