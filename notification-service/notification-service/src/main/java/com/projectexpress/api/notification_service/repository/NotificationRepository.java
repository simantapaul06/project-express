package com.projectexpress.api.notification_service.repository;

import com.projectexpress.api.notification_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);
    List<Notification> findByCandidateIdAndReadFalseOrderByCreatedAtDesc(Long candidateId);
    Optional<Notification> findByIdAndCandidateId(Long id, Long candidateId);
}