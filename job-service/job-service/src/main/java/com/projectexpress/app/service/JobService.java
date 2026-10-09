package com.projectexpress.app.service;

import com.projectexpress.app.dto.JobRequest;
import com.projectexpress.app.dto.JobResponse;
import com.projectexpress.app.dto.JobUpdateRequest;
import com.projectexpress.app.entity.Job;
import com.projectexpress.app.entity.Location;
import com.projectexpress.app.exception.ResourceNotFoundException;
import com.projectexpress.app.mapper.JobMapper;
import com.projectexpress.app.repository.JobRepository;
import com.projectexpress.app.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final LocationRepository locationRepository;
    private final JobMapper jobMapper;


    //create Job
    @Caching(evict = {
            @CacheEvict(value = "jobs", allEntries = true),
            @CacheEvict(value = "jobById", allEntries = true)
    })
    public JobResponse createJob(JobRequest request, Long createdBy) {

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Location not found"));

        Job job = Job.builder()
                .title(request.getTitle())
                .company(request.getCompany())
                .description(request.getDescription())
                .employmentType(request.getEmploymentType())
                .experienceRequired(request.getExperienceRequired())
                .salary(request.getSalary())
                .location(location)
                .createdBy(createdBy)
                .build();
        Job savedJob = jobRepository.save(job);

        return jobMapper.toJobResponse(savedJob);
    }

    //retrieve all jobs
    @Cacheable(value = "jobs", key = "'all'")
    @Transactional(readOnly = true)
    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll()
                .stream()
                .map(jobMapper::toJobResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    //get by id
    @Cacheable(value = "jobById", key = "#id")
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() ->
                new ResourceNotFoundException("Job not found with id: " + id));
        return jobMapper.toJobResponse(job);
    }

    //update
    @Caching(evict = {
            @CacheEvict(value = "jobs", allEntries = true),
            @CacheEvict(value = "jobById", allEntries = true)
    })
    public JobResponse updateJob(Long id, JobUpdateRequest request) {

        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        existingJob.setTitle(request.getTitle());
        existingJob.setCompany(request.getCompany());
        existingJob.setDescription(request.getDescription());
        existingJob.setEmploymentType(request.getEmploymentType());
        existingJob.setExperienceRequired(request.getExperienceRequired());
        existingJob.setSalary(request.getSalary());
        existingJob.setLocation(location);

        Job updatedJob = jobRepository.save(existingJob);
        return jobMapper.toJobResponse(updatedJob);
    }


    //delete by id
    @Caching(evict = {
            @CacheEvict(value = "jobs", allEntries = true),
            @CacheEvict(value = "jobById", allEntries = true)
    })
    public void deleteJob(Long id) {

        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        jobRepository.delete(existingJob);
    }
}