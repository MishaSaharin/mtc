package com.saccharine.mtc.services;

import com.saccharine.mtc.entities.Account;
import com.saccharine.mtc.entities.User;
import com.saccharine.mtc.repositories.AccountRepository;
import com.saccharine.mtc.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // Негативный сценарий: username == null
    @Test
    void registerUser_ShouldThrowException_WhenUsernameIsNull() {
        // Arrange
        String username = null;
        String password = "password";
        User.Role role = User.Role.ROLE_USER;

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(username, password, role))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Имя пользователя и пароль не должны быть пустыми");
    }

    // Негативный сценарий: password пустой
    @Test
    void registerUser_ShouldThrowException_WhenPasswordIsBlank() {
        // Arrange
        String username = "testUser";
        String password = "   "; // пустая строка
        User.Role role = User.Role.ROLE_USER;

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(username, password, role))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Имя пользователя и пароль не должны быть пустыми");
    }

    // Негативный сценарий: пользователь с таким именем уже существует
    @Test
    void registerUser_ShouldThrowException_WhenUserAlreadyExists() {
        // Arrange
        String username = "existingUser";
        String password = "password";
        User.Role role = User.Role.ROLE_USER;
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(username, password, role))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Пользователь с таким именем уже существует");
    }

    // Позитивный сценарий: корректная регистрация пользователя
    @Test
    void registerUser_ShouldRegisterUser_WhenValidDataProvided() {
        // Arrange
        String username = "newUser";
        String password = "password";
        User.Role role = User.Role.ROLE_USER;
        // Предполагаем, что такого пользователя ещё нет
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        // Симуляция кодирования пароля
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        // Симуляция сохранения пользователя (например, с присвоением id)
        User savedUser = new User();
        savedUser.setUsername(username);
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(role);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.registerUser(username, password, role);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getRole()).isEqualTo(role);

        // Проверяем, что методы репозиториев были вызваны
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any(Account.class));
    }

    // Позитивный сценарий: метод findByUsername возвращает пользователя
    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findByUsername(username);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
    }

    // Негативный сценарий: метод findByUsername возвращает пустой Optional, если пользователь не найден
    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Arrange
        String username = "nonExistingUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByUsername(username);

        // Assert
        assertThat(result).isEmpty();
    }
}