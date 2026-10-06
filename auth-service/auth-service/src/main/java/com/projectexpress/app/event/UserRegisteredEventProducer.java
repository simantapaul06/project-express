package com.projectexpress.app.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisteredEventProducer {

    private static final String TOPIC = "user-registered";

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.getUserId().toString(),
                event
        );
    }
}