package com.projectexpress.application_service.service;

import com.projectexpress.application_service.client.JobServiceClient;
import com.projectexpress.application_service.exception.JobNotFoundException;
import com.projectexpress.application_service.exception.JobServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobValidationService {

    private final JobServiceClient jobServiceClient;

    @Retry(name = "jobService")
    @CircuitBreaker(name = "jobService", fallbackMethod = "jobServiceFallback")
    public void validateJob(Long jobId, String authorizationHeader) {
        try {
            jobServiceClient.validateJobExists(jobId, authorizationHeader);
        } catch (FeignException.NotFound e) {
            throw new JobNotFoundException("Job not found with id: " + jobId);
        }
    }

    public void jobServiceFallback(Long jobId, String authorizationHeader, Throwable throwable){

        if(throwable instanceof JobNotFoundException jobNotFoundException){
            throw jobNotFoundException;
        }

        throw new JobServiceUnavailableException("Job Service is currently unable, please try it later.");
    }

}
