package org.example.kafka;

import org.example.dto.UserEvent;
import org.example.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    private final NotificationService notificationService;

    public UserEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserEvent(UserEvent event) {
        log.info("Получено событие из Kafka: email={}, operation={}",
                event.getEmail(), event.getOperation());
        notificationService.sendNotification(event.getEmail(), event.getOperation());
    }
}
