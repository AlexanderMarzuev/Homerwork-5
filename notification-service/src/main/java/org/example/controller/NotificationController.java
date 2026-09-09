package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.SendNotificationRequest;
import org.example.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody @Valid SendNotificationRequest request) {
        notificationService.sendNotification(request.getEmail(), request.getOperation());
        return ResponseEntity.ok("Уведомление отправлено на " + request.getEmail());
    }
}
