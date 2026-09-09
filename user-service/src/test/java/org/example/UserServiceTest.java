package org.example;


import org.example.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventProducer eventProducer;

    @Test
    void create_shouldSaveUserAndPublishEvent() {
        User input = new User(null, "Alex", "alex@gmail.com", 30);
        User expected = new User(1L, "Alex", "alex@gmail.com", 30);
        when(userRepository.save(any())).thenReturn(expected);

        UserResponse result = userService.create(input);

        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
        verify(eventProducer).sendUserEvent("alex@gmail.com", UserEvent.UserOperation.CREATED);
    }

    @Test
    void findById_shouldReturnUser() {
        User user = new User(1L, "Kate", "kate@ya.ru", 25);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.findById(1L);

        assertThat(result.getName()).isEqualTo("Kate");
        verify(userRepository).findById(1L);
    }

    @Test
    void delete_shouldRemoveUserAndPublishEvent() {
        User user = new User(1L, "Bob", "bob@ya.ru", 40);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
        verify(eventProducer).sendUserEvent("bob@ya.ru", UserEvent.UserOperation.DELETED);
    }
}
