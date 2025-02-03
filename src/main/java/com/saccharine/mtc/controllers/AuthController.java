package com.saccharine.mtc.controllers;

import com.saccharine.mtc.authentication.AuthRequest;
import com.saccharine.mtc.authentication.AuthResponse;
import com.saccharine.mtc.authentication.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        // Аутентификация пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        // Генерация JWT-токена
        String token = jwtUtil.generateToken(authRequest.getUsername());

        // Возвращаем токен в ответе
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        return ResponseEntity.ok(authResponse);
    }
}