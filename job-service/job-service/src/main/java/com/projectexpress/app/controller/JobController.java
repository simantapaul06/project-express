package com.projectexpress.app.controller;

import com.projectexpress.app.dto.JobRequest;
import com.projectexpress.app.dto.JobResponse;
import com.projectexpress.app.dto.JobUpdateRequest;
import com.projectexpress.app.entity.Job;
import com.projectexpress.app.mapper.JobMapper;
import com.projectexpress.app.security.AuthenticatedUser;
import com.projectexpress.app.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobMapper jobMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request, Authentication authentication) {

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

        Job job = jobService.createJob(request,authenticatedUser.getUserId());
        return ResponseEntity.ok(jobMapper.toJobResponse(job));
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {

        List<JobResponse> jobs = jobService.getAllJobs()
                .stream()
                .map(jobMapper::toJobResponse)
                .toList();

        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {

        Job job = jobService.getJobById(id);
        return ResponseEntity.ok(jobMapper.toJobResponse(job));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id, @Valid @RequestBody JobUpdateRequest request) {

        Job job = jobService.updateJob(id, request);

        return ResponseEntity.ok(jobMapper.toJobResponse(job));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobById(@PathVariable Long id) {

        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
