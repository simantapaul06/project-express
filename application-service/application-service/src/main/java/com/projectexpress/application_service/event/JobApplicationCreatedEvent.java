package com.projectexpress.application_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationCreatedEvent {

    private Long applicationId;
    private Long candidateId;
    private Long jobId;
    private String status;
    private LocalDateTime appliedAt;
}