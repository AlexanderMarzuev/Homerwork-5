package org.example.service;

import org.example.dto.UserEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final String SITE_NAME = "Alexander.ru";

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public NotificationService(JavaMailSender mailSender,
                               @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendNotification(String email, UserEvent.UserOperation operation) {
        String subject;
        String text;

        if (operation == UserEvent.UserOperation.CREATED) {
            subject = "Аккаунт создан";
            text = "Здравствуйте! Ваш аккаунт на сайте " + SITE_NAME + " был успешно создан.";
        } else {
            subject = "Аккаунт удалён";
            text = "Здравствуйте! Ваш аккаунт был удалён.";
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
