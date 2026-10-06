package com.projectexpress.application_service.mapper;

import com.projectexpress.application_service.dto.ApplicationResponse;
import com.projectexpress.application_service.entity.JobApplication;
import org.springframework.stereotype.Component;

@Component
public class JobApplicationMapper {

    public ApplicationResponse toResponse(JobApplication application) {

        return new ApplicationResponse(
                application.getId(),
                application.getJobId(),
                application.getCandidateId(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getUpdatedAt()
        );
    }

}
