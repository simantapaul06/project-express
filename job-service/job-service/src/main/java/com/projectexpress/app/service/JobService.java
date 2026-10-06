package com.projectexpress.app.service;

import com.projectexpress.app.dto.JobRequest;
import com.projectexpress.app.dto.JobUpdateRequest;
import com.projectexpress.app.entity.Job;
import com.projectexpress.app.entity.Location;
import com.projectexpress.app.exception.ResourceNotFoundException;
import com.projectexpress.app.repository.JobRepository;
import com.projectexpress.app.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final LocationRepository locationRepository;


    //create Job
    public Job createJob(JobRequest request, Long createdBy) {

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

        return jobRepository.save(job);
    }

    //retrieve all jobs
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    //get by id
    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    //update
    public Job updateJob(Long id, JobUpdateRequest request) {

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

        return jobRepository.save(existingJob);
    }


    //delete by id
    public void deleteJob(Long id) {

        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        jobRepository.delete(existingJob);
    }
}