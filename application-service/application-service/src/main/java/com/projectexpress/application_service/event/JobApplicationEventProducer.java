package com.projectexpress.application_service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobApplicationEventProducer {
    private static final String TOPIC = "job-application-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendApplicationCreateEvent(JobApplicationCreatedEvent event){

        kafkaTemplate.send(

                TOPIC,
                event.getCandidateId().toString(), //kafka message key
                event
        );

    }



}
