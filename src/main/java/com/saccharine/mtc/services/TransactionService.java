package com.saccharine.mtc.services;

import com.saccharine.mtc.entities.Account;
import com.saccharine.mtc.repositories.AccountRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;

    public TransactionService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Async
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void optimisticTransferMoney(UUID senderId, UUID receiverId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }

        Account senderAccount = accountRepository.findByUserId(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Счет отправителя не найден"));
        Account receiverAccount = accountRepository.findByUserId(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Счет получателя не найден"));

        if (!senderAccount.withdraw(amount)) {
            throw new IllegalStateException("Недостаточно средств на счете отправителя");
        }
        receiverAccount.deposit(amount);

        try {
            accountRepository.save(senderAccount);
            accountRepository.save(receiverAccount);
        } catch (OptimisticLockException ex) {
            throw new OptimisticLockException("Ошибка оптимистичной блокировки. Попробуйте повторить операцию позже.", ex);
        }
    }

    @Async
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void pessimisticTransferMoney(UUID senderId, UUID receiverId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }

        UUID firstLockId, secondLockId;
        if (senderId.compareTo(receiverId) <= 0) {
            firstLockId = senderId;
            secondLockId = receiverId;
        } else {
            firstLockId = receiverId;
            secondLockId = senderId;
        }

        Account firstAccount = accountRepository.findByUserIdForUpdate(firstLockId)
                .orElseThrow(() -> new IllegalArgumentException("Счет для пользователя " + firstLockId + " не найден"));
        Account secondAccount = accountRepository.findByUserIdForUpdate(secondLockId)
                .orElseThrow(() -> new IllegalArgumentException("Счет для пользователя " + secondLockId + " не найден"));

        Account senderAccount = senderId.equals(firstLockId) ? firstAccount : secondAccount;
        Account receiverAccount = senderId.equals(firstLockId) ? secondAccount : firstAccount;

        if (!senderAccount.withdraw(amount)) {
            throw new IllegalStateException("Недостаточно средств на счете отправителя");
        }
        receiverAccount.deposit(amount);

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        CompletableFuture.completedFuture(null);
    }
}