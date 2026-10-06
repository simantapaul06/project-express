package com.projectexpress.application_service.service;

import com.projectexpress.application_service.client.ProfileClient;
import com.projectexpress.application_service.dto.ApplicationRequest;
import com.projectexpress.application_service.entity.ApplicationStatus;
import com.projectexpress.application_service.entity.JobApplication;
import com.projectexpress.application_service.event.JobApplicationEventProducer;
import com.projectexpress.application_service.exception.DuplicateApplicationException;
import com.projectexpress.application_service.repository.ApplicationStatusHistoryRepository;
import com.projectexpress.application_service.repository.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private JobApplicationEventProducer eventProducer;

    @Mock
    private JobValidationService jobValidationService;

    @Mock
    private ProfileClient profileClient;

    @Mock
    private ApplicationStatusHistoryRepository applicationStatusHistoryRepository;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    // ---------------------------------------------------------------
    // 1. First-time application
    // ---------------------------------------------------------------
    @Test
    void shouldApplyForJobSuccessfully() {

        //ARRANGE
        Long candidateId = 21L;
        Long jobId = 7L;
        String authorizationHeader = "Bearer test-token";

        ApplicationRequest request = new ApplicationRequest(jobId);

        when(jobApplicationRepository.findByCandidateIdAndJobId(candidateId, request.jobId()))
                .thenReturn(Optional.empty());

        JobApplication application = JobApplication.builder()
                .id(19L)
                .jobId(request.jobId())
                .candidateId(candidateId)
                .status(ApplicationStatus.APPLIED)
                .build();

        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(application);


        //ACT
        JobApplication result = jobApplicationService.apply(request, candidateId, authorizationHeader);

        //ASSERT
        assertNotNull(result);
        assertEquals(19L, result.getId());
        assertEquals(jobId, result.getJobId());
        assertEquals(candidateId, result.getCandidateId());
        assertEquals(ApplicationStatus.APPLIED, result.getStatus());



    }


    // ---------------------------------------------------------------
    // 2. Duplicate application (already APPLIED)
    // ---------------------------------------------------------------
    @Test
    void shouldThrowExceptionWhenCandidateAlreadyApplied() {

        // ARRANGE
        Long candidateId = 21L;
        Long jobId = 7L;
        String authorizationHeader = "Bearer test-token";

        ApplicationRequest request = new ApplicationRequest(jobId);

        JobApplication existingApplication = JobApplication.builder()
                .id(19L)
                .jobId(jobId)
                .candidateId(candidateId)
                .status(ApplicationStatus.APPLIED)
                .build();

        when(jobApplicationRepository.findByCandidateIdAndJobId(candidateId, request.jobId()))
                .thenReturn(Optional.of(existingApplication));


        // ACT + ASSERT
        DuplicateApplicationException duplicateApplicationException = assertThrows(DuplicateApplicationException.class,
                () -> jobApplicationService.apply(request, candidateId, authorizationHeader)
                );

        assertEquals("You have already applied for this job", duplicateApplicationException.getMessage());

    }

    // ---------------------------------------------------------------
    // 3. Re-application after WITHDRAWN
    // ---------------------------------------------------------------
    @Test
    void shouldAllowReapplicationAfterWithdrawal() {

        // ARRANGE
        Long candidateId = 21L;
        Long jobId = 7L;
        String authorizationHeader = "Bearer test-token";

        ApplicationRequest request = new ApplicationRequest(jobId);

        JobApplication withdrawnApplication = JobApplication.builder()
                .id(19L)
                .jobId(jobId)
                .candidateId(candidateId)
                .status(ApplicationStatus.WITHDRAWN)
                .build();

        when(jobApplicationRepository.findByCandidateIdAndJobId(candidateId, jobId)).thenReturn(Optional.of(withdrawnApplication));

        // Here the service saves the SAME object it got from the repository,
        // so stubbing with the object itself also works (like in the YouTube example).
        when(jobApplicationRepository.save(withdrawnApplication)).thenReturn(withdrawnApplication);
//        when(jobApplicationRepository.save(any(JobApplication.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        JobApplication result = jobApplicationService.apply(request, candidateId, authorizationHeader);

        // ASSERT
        assertNotNull(result);
        assertEquals(19L, result.getId());
        assertEquals(jobId, result.getJobId());
        assertEquals(candidateId, result.getCandidateId());
        assertEquals(ApplicationStatus.APPLIED, result.getStatus());


        // VERIFY
        verify(jobApplicationRepository).save(withdrawnApplication);

        verify(applicationStatusHistoryRepository).save(any());
    }


    @Test
    void shouldWithdrawApplicationSuccessfully() {

        // ARRANGE
        Long applicationId = 19L;
        Long candidateId = 21L;

        JobApplication existingApplication = JobApplication.builder()
                .id(applicationId)
                .jobId(7L)
                .candidateId(candidateId)
                .status(ApplicationStatus.APPLIED)
                .build();

        when(jobApplicationRepository.findByIdAndCandidateId(applicationId, candidateId))
                .thenReturn(Optional.of(existingApplication));

        when(jobApplicationRepository.save(any(JobApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        // ACT
        JobApplication result = jobApplicationService.withdrawApplication(applicationId, candidateId);


        // ASSERT
        assertNotNull(result);
        assertEquals(applicationId, result.getId());
        assertEquals(candidateId, result.getCandidateId());
        assertEquals(ApplicationStatus.WITHDRAWN, result.getStatus());


        // VERIFY
        verify(jobApplicationRepository).save(existingApplication);

        verify(applicationStatusHistoryRepository).save(any());
    }


    //trying to withdraw an already withdrawn application
    @Test
    void shouldThrowExceptionWhenApplicationAlreadyWithdrawn() {

        // ARRANGE
        Long applicationId = 19L;
        Long candidateId = 21L;

        JobApplication existingApplication = JobApplication.builder()
                .id(applicationId)
                .jobId(7L)
                .candidateId(candidateId)
                .status(ApplicationStatus.WITHDRAWN)
                .build();

        when(jobApplicationRepository.findByIdAndCandidateId(applicationId, candidateId))
                .thenReturn(Optional.of(existingApplication));


        // ACT + ASSERT
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> jobApplicationService.withdrawApplication(
                        applicationId,
                        candidateId
                )
        );

        assertEquals(
                "Application is already withdrawn",
                exception.getMessage()
        );


        // VERIFY
        verify(jobApplicationRepository, never()).save(any(JobApplication.class)); //Make sure save() was NOT called.

        verify(applicationStatusHistoryRepository, never()).save(any());
    }

    //an application in a final state (SELECTED) cannot be withdrawn.
    @Test
    void shouldNotAllowWithdrawalWhenApplicationIsSelected() {

        // ARRANGE
        Long applicationId = 19L;
        Long candidateId = 21L;

        JobApplication existingApplication = JobApplication.builder()
                .id(applicationId)
                .jobId(7L)
                .candidateId(candidateId)
                .status(ApplicationStatus.SELECTED)
                .build();

        when(jobApplicationRepository.findByIdAndCandidateId(applicationId, candidateId))
                .thenReturn(Optional.of(existingApplication));


        // ACT + ASSERT
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> jobApplicationService.withdrawApplication(
                        applicationId,
                        candidateId
                )
        );

        assertEquals(
                "Application cannot be withdrawn because its current status is SELECTED",
                exception.getMessage()
        );


        // VERIFY
        verify(jobApplicationRepository, never()).save(any(JobApplication.class));

        verify(applicationStatusHistoryRepository, never()).save(any());
    }



    @Test
    void shouldUpdateApplicationStatusSuccessfully() {

        // ARRANGE
        Long applicationId = 19L;

        JobApplication existingApplication = JobApplication.builder()
                .id(applicationId)
                .jobId(7L)
                .candidateId(21L)
                .status(ApplicationStatus.APPLIED)
                .build();

        when(jobApplicationRepository.findById(applicationId)).thenReturn(Optional.of(existingApplication));

        when(jobApplicationRepository.save(any(JobApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        // ACT
        JobApplication result = jobApplicationService.updateApplicationStatus(
                        applicationId,
                        ApplicationStatus.UNDER_REVIEW
                );


        // ASSERT
        assertNotNull(result);
        assertEquals(ApplicationStatus.UNDER_REVIEW, result.getStatus());


        // VERIFY
        verify(jobApplicationRepository).save(existingApplication);

        verify(applicationStatusHistoryRepository).save(any());
    }

    @Test
    void shouldThrowExceptionForInvalidStatusTransition() {

        // ARRANGE
        Long applicationId = 19L;

        JobApplication existingApplication = JobApplication.builder()
                .id(applicationId)
                .jobId(7L)
                .candidateId(21L)
                .status(ApplicationStatus.UNDER_REVIEW)
                .build();

        when(jobApplicationRepository.findById(applicationId))
                .thenReturn(Optional.of(existingApplication));


        // ACT + ASSERT
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> jobApplicationService.updateApplicationStatus(
                        applicationId,
                        ApplicationStatus.SELECTED
                )
        );

        assertEquals(
                "Invalid application status transition: UNDER_REVIEW -> SELECTED",
                exception.getMessage()
        );


        // VERIFY
        verify(jobApplicationRepository, never()).save(any(JobApplication.class));

        verify(applicationStatusHistoryRepository, never()).save(any());
    }
}