package kafka;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.example.dto.UserEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"user-events"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext
class UserEventConsumerIT {

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
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void shouldSendEmailOnUserCreatedEvent() throws Exception {
        UserEvent event = new UserEvent("newuser@example.com", UserEvent.UserOperation.CREATED);

        kafkaTemplate.send("user-events", event);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertThat(messages).hasSize(1);

            assertThat(messages[0].getSubject()).isEqualTo("Аккаунт создан");
            assertThat(messages[0].getContent().toString())
                    .contains("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");
        });
    }

    @Test
    void shouldSendEmailOnUserDeletedEvent() throws Exception {
        UserEvent event = new UserEvent("deleted@example.com", UserEvent.UserOperation.DELETED);

        kafkaTemplate.send("user-events", event);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertThat(messages).hasSize(1);

            assertThat(messages[0].getSubject()).isEqualTo("Аккаунт удалён");
            assertThat(messages[0].getContent().toString())
                    .contains("Здравствуйте! Ваш аккаунт был удалён.");
        });
    }
}
