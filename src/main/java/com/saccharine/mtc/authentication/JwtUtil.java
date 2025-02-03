package com.saccharine.mtc.authentication;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Генерация ключа

    // Генерация JWT-токена
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // Устанавливаем subject (обычно username)
                .setIssuedAt(new Date()) // Время создания токена
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Срок действия токена (10 часов)
                .signWith(key) // Подписываем токен
                .compact();
    }

    // Извлечение username из токена
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Валидация токена
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}