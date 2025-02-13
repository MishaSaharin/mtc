package com.saccharine.mtc.services;

import com.saccharine.mtc.entities.User;
import com.saccharine.mtc.repositories.AccountRepository;
import com.saccharine.mtc.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UserService userService;

    private BCryptPasswordEncoder passwordEncoder;
    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        user = new User();
        user.setUsername("user1");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(User.Role.ROLE_USER);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
    }

    @Test
    public void testRegisterUser() {
        User registeredUser = userService.registerUser("user1", "password123", User.Role.ROLE_USER);

        assertNotNull(registeredUser);
        assertEquals("user1", registeredUser.getUsername());
        assertTrue(passwordEncoder.matches("password123", registeredUser.getPassword()));
        assertEquals(User.Role.ROLE_USER, registeredUser.getRole());
    }

    @Test
    public void testRegisterUserAlreadyExists() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            userService.registerUser("user1", "password123", User.Role.ROLE_USER);
        });

        assertEquals("Пользователь с таким именем уже существует", exception.getMessage());
    }
}