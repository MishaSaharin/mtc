package com.saccharine.mtc.controllers;

import com.saccharine.mtc.services.TransactionService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer/optimistic")
    public ResponseEntity<Map<String, String>> transferOptimistic(@RequestParam @NotNull UUID senderId,
                                                                  @RequestParam @NotNull UUID receiverId,
                                                                  @RequestParam @NotNull BigDecimal amount) {
        transactionService.optimisticTransferMoney(senderId, receiverId, amount);
        return ResponseEntity.ok(Map.of("message", "Оптимистичный перевод выполнен успешно"));
    }


    @PostMapping("/transfer/pessimistic")
    public ResponseEntity<Map<String, String>> transferPessimistic(@RequestParam @NotNull UUID senderId,
                                                                   @RequestParam @NotNull UUID receiverId,
                                                                   @RequestParam @NotNull BigDecimal amount) {
        transactionService.pessimisticTransferMoney(senderId, receiverId, amount);
        return ResponseEntity.ok(Map.of("message", "Пессимистичный перевод выполнен успешно"));
    }
}