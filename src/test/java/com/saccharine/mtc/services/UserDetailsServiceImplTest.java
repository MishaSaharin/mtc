package com.saccharine.mtc.services;

import com.saccharine.mtc.entities.User;
import com.saccharine.mtc.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private UUID userId;
    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        user = new User();
        user.setUsername("user1");
        user.setPassword("encodedpassword");
        user.setRole(User.Role.ROLE_USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

//    @Test
//    public void testLoadUserByUsername() {
//        String userIdStr = userId.toString();
//        UserDetails userDetails = userDetailsService.loadUserByUsername(userIdStr);
//
//        assertNotNull(userDetails);
//        assertEquals(userId.toString(), userDetails.getUsername());
//        assertTrue(userDetails.getAuthorities().stream()
//                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
//    }

    @Test
    public void testLoadUserByUsernameNotFound() {
        String nonExistentUserId = UUID.randomUUID().toString();
        when(userRepository.findById(UUID.fromString(nonExistentUserId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(nonExistentUserId);
        });

        assertEquals("Пользователь не найден", exception.getMessage());
    }
}