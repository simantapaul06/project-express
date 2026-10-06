package com.projectexpress.app.mapper;

import com.projectexpress.app.dto.JobResponse;
import com.projectexpress.app.entity.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobMapper {
    private final LocationMapper locationMapper;

    public JobResponse toJobResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getDescription(),
                job.getEmploymentType(),
                job.getExperienceRequired(),
                job.getSalary(),
                locationMapper.toResponse(job.getLocation()),
                job.getCreatedAt(),
                job.getUpdatedAt(),
                job.getCreatedBy()
        );
    }
}
