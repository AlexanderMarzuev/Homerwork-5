package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.example.dto.SendNotificationRequest;
import org.example.dto.UserEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class NotificationControllerIT {

    @RegisterExtension
    static GreenMailExtension greenMail =
            new GreenMailExtension(ServerSetupTest.SMTP)
                    .withConfiguration(GreenMailConfiguration.aConfig().withUser("test@localhost", "12345"));

    @DynamicPropertySource
    static void mailProperties(DynamicPropertyRegistry registry) {
        greenMail.start();
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> greenMail.getSmtp().getPort());
        registry.add("spring.mail.username", () -> "test@localhost");
        registry.add("spring.mail.password", () -> "12345");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendNotificationViaApi_shouldSendCreatedEmail() throws Exception {
        SendNotificationRequest request =
                new SendNotificationRequest("api-created@example.com", UserEvent.UserOperation.CREATED);

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Уведомление отправлено на api-created@example.com"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertThat(messages).hasSize(1);
            assertThat(messages[0].getSubject()).isEqualTo("Аккаунт создан");
            assertThat(messages[0].getContent().toString())
                    .contains("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");
        });
    }

    @Test
    void sendNotificationViaApi_shouldSendDeletedEmail() throws Exception {
        SendNotificationRequest request =
                new SendNotificationRequest("api-deleted@example.com", UserEvent.UserOperation.DELETED);

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Уведомление отправлено на api-deleted@example.com"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertThat(messages).hasSize(1);
            assertThat(messages[0].getSubject()).isEqualTo("Аккаунт удалён");
            assertThat(messages[0].getContent().toString())
                    .contains("Здравствуйте! Ваш аккаунт был удалён.");
        });
    }

    @Test
    void sendNotificationViaApi_shouldReturnBadRequestForInvalidEmail() throws Exception {
        SendNotificationRequest request =
                new SendNotificationRequest("not-an-email", UserEvent.UserOperation.CREATED);

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
