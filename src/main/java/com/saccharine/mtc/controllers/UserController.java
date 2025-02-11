package com.saccharine.mtc.controllers;

import com.saccharine.mtc.authentication.JwtUtil;
import com.saccharine.mtc.entities.User;
import com.saccharine.mtc.services.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestParam @NotBlank String username,
                                                        @RequestParam @NotBlank String password) {
        User user = userService.registerUser(username, password, User.Role.ROLE_USER);
        return ResponseEntity.ok(Map.of("message", "Пользователь успешно зарегистрирован",
                "userId", user.getId().toString()));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam @NotBlank String username,
                                                     @RequestParam @NotBlank String password) {
        Optional<User> optionalUser = userService.findByUsername(username);
        if (optionalUser.isEmpty() || !passwordEncoder.matches(password, optionalUser.get().getPassword())) {
            throw new IllegalArgumentException("Неверные учетные данные");
        }
        User user = optionalUser.get();
        String token = jwtUtil.generateToken(user.getId(), user.getRole().name());
        return ResponseEntity.ok(Map.of("token", token));
    }
}