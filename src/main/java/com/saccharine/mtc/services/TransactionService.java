package com.saccharine.mtc.services;

import com.saccharine.mtc.entities.Account;
import com.saccharine.mtc.repositories.AccountRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;

    public TransactionService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
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

    @Transactional
    public void pessimisticTransferMoney(UUID senderId, UUID receiverId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }

        Account senderAccount = accountRepository.findByUserIdForUpdate(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Счет отправителя не найден"));
        Account receiverAccount = accountRepository.findByUserIdForUpdate(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Счет получателя не найден"));

        if (!senderAccount.withdraw(amount)) {
            throw new IllegalStateException("Недостаточно средств на счете отправителя");
        }
        receiverAccount.deposit(amount);

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);
    }
}