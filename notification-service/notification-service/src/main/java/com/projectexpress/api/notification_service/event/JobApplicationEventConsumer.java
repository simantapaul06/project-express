package com.projectexpress.api.notification_service.event;

import com.projectexpress.api.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobApplicationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "job-application-created", groupId = "notification-service-group")
    public void consume(JobApplicationCreatedEvent event) {
        System.out.println("=================================");
        System.out.println("Job Application Event Received");
        System.out.println("Application ID: " + event.getApplicationId());
        System.out.println("Candidate ID: " + event.getCandidateId());
        System.out.println("Job ID: " + event.getJobId());
        System.out.println("Status: " + event.getStatus());
        System.out.println("Applied At: " + event.getAppliedAt());
        System.out.println("=================================");

        notificationService.createApplicationNotification(event);
    }

}
