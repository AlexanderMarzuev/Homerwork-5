package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String topic;

    public UserEventProducer(KafkaTemplate<String, UserEvent> kafkaTemplate,
                             @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendUserEvent(String email, UserEvent.UserOperation operation) {
        UserEvent event = new UserEvent(email, operation);
        kafkaTemplate.send(topic, event);
    }
}
