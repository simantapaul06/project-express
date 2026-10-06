package com.projectexpress.api.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationCreatedEvent {

    private Long applicationId;
    private Long candidateId;
    private Long jobId;
    private String status;
    private LocalDateTime appliedAt;

}
