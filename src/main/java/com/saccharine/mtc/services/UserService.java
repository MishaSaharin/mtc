package com.saccharine.mtc.services;

import com.saccharine.mtc.entities.Account;
import com.saccharine.mtc.entities.User;
import com.saccharine.mtc.repositories.AccountRepository;
import com.saccharine.mtc.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String username, String password, User.Role role) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя и пароль не должны быть пустыми");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user = userRepository.save(user);

        Account account = new Account();
        account.setUser(user);
        account.deposit(BigDecimal.ZERO);
        accountRepository.save(account);

        return user;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}