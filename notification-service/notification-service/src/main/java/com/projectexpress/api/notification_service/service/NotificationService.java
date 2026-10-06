package com.projectexpress.api.notification_service.service;

import com.projectexpress.api.notification_service.entity.Notification;
import com.projectexpress.api.notification_service.entity.NotificationType;
import com.projectexpress.api.notification_service.event.JobApplicationCreatedEvent;
import com.projectexpress.api.notification_service.exception.NotificationNotFoundException;
import com.projectexpress.api.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createApplicationNotification(JobApplicationCreatedEvent event) {

        Notification notification = Notification.builder()
                .candidateId(event.getCandidateId())
                .applicationId(event.getApplicationId())
                .jobId(event.getJobId())
                .type(NotificationType.APPLICATION_CREATED)
                .message(
                        "Your application for Job "
                                + event.getJobId()
                                + " has been submitted successfully."
                )
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(Long candidateId) {
        return notificationRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId);
    }

    public List<Notification> getUnreadNotifications(Long candidateId) {
        return notificationRepository.findByCandidateIdAndReadFalseOrderByCreatedAtDesc(candidateId);
    }

    public Notification markAsRead(Long notificationId, Long candidateId) {

        Notification notification = notificationRepository.findByIdAndCandidateId(notificationId, candidateId)
                .orElseThrow(() -> new NotificationNotFoundException(
                                "Notification not found with id: "
                                        + notificationId
                        ));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(Long candidateId) {
        List<Notification> notifications = notificationRepository.findByCandidateIdAndReadFalseOrderByCreatedAtDesc(candidateId);
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}