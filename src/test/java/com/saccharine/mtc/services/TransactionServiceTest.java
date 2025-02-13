package com.saccharine.mtc.services;

import com.saccharine.mtc.entities.Account;
import com.saccharine.mtc.entities.User;
import com.saccharine.mtc.repositories.AccountRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;
    private UUID senderId;
    private UUID receiverId;
    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        User sender = new User();
        User receiver = new User();

        senderId = sender.getId();
        receiverId = receiver.getId();

        senderAccount = new Account();
        senderAccount.setUser(sender);
        senderAccount.deposit(new BigDecimal("100.00"));

        receiverAccount = new Account();
        receiverAccount.setUser(receiver);
        receiverAccount.deposit(new BigDecimal("50.00"));

        when(accountRepository.findByUserId(senderId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(receiverId)).thenReturn(Optional.of(receiverAccount));
    }

    @Test
    public void testOptimisticTransferSuccess() {
        transactionService.optimisticTransferMoney(senderId, receiverId, new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("80.00"), receiverAccount.getBalance());
    }

    @Test
    public void testOptimisticTransferInsufficientFunds() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            transactionService.optimisticTransferMoney(senderId, receiverId, new BigDecimal("200.00"));
        });

        assertEquals("Недостаточно средств на счете отправителя", exception.getMessage());
    }

    @Test
    public void testPessimisticTransferSuccess() {
        transactionService.pessimisticTransferMoney(senderId, receiverId, new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("80.00"), receiverAccount.getBalance());
    }

    @Test
    public void testPessimisticTransferInsufficientFunds() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            transactionService.pessimisticTransferMoney(senderId, receiverId, new BigDecimal("200.00"));
        });

        assertEquals("Недостаточно средств на счете отправителя", exception.getMessage());
    }

    @Test
    public void testOptimisticLockingConflict() {
        Exception exception = assertThrows(OptimisticLockException.class, () -> {
            transactionService.optimisticTransferMoney(senderId, receiverId, new BigDecimal("30.00"));
        });

        assertEquals("Ошибка оптимистичной блокировки. Попробуйте повторить операцию позже.", exception.getMessage());
    }
}