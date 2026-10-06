package com.projectexpress.api.profile_service.event;

import com.projectexpress.api.profile_service.entity.CandidateProfile;
import com.projectexpress.api.profile_service.repository.CandidateProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisteredEventConsumer {

    private final CandidateProfileRepository candidateProfileRepository;

    @KafkaListener(
            topics = "user-registered",
            groupId = "profile-service"
    )
    public void consume(UserRegisteredEvent event) {

        // Avoid creating duplicate profiles
        if (candidateProfileRepository.findByCandidateId(event.getUserId()).isPresent()) {
            return;
        }

        CandidateProfile profile = CandidateProfile.builder()
                .candidateId(event.getUserId())
                .name(event.getName())
                .email(event.getEmail())
                .build();

        candidateProfileRepository.save(profile);

        System.out.println("Basic candidate profile created for candidateId: " + event.getUserId());
    }
}