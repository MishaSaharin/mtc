package com.saccharine.mtc.services;

import com.saccharine.mtc.entities.User;
import com.saccharine.mtc.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    // Позитивный сценарий: пользователь найден и корректно преобразован в UserDetails
    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String userIdString = userId.toString();
        User user = new User();
        user.setId(userId);
        user.setPassword("encodedPassword");
        user.setRole(User.Role.ROLE_USER);  // Предполагается, что у вас есть enum User.Role

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(userIdString);

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(userIdString);
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .containsExactly("USER");

        verify(userRepository).findById(userId);
    }

    // Негативный сценарий: неверный формат идентификатора пользователя
    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserIdFormatIsInvalid() {
        // Arrange
        String invalidUserId = "invalid-uuid";

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(invalidUserId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Неверный формат идентификатора пользователя");
    }

    // Негативный сценарий: пользователь не найден
    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String userIdString = userId.toString();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(userIdString))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Пользователь не найден");

        verify(userRepository).findById(userId);
    }
}