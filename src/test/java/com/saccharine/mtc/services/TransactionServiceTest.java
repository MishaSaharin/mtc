package com.saccharine.mtc.services;

import com.saccharine.mtc.entities.Account;
import com.saccharine.mtc.repositories.AccountRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    // ===================== Тесты для optimisticTransferMoney =====================

    // Негативный сценарий: сумма перевода null
    @Test
    void optimisticTransferMoney_ShouldThrowException_WhenAmountIsNull() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = null;

        // Act & Assert
        assertThatThrownBy(() -> transactionService.optimisticTransferMoney(senderId, receiverId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сумма перевода должна быть положительной");
    }

    // Негативный сценарий: сумма перевода меньше или равна нулю
    @Test
    void optimisticTransferMoney_ShouldThrowException_WhenAmountIsZeroOrNegative() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal zeroAmount = BigDecimal.ZERO;
        BigDecimal negativeAmount = BigDecimal.valueOf(-10);

        // Act & Assert for zero
        assertThatThrownBy(() -> transactionService.optimisticTransferMoney(senderId, receiverId, zeroAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сумма перевода должна быть положительной");

        // Act & Assert for negative value
        assertThatThrownBy(() -> transactionService.optimisticTransferMoney(senderId, receiverId, negativeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сумма перевода должна быть положительной");
    }

    // Негативный сценарий: Счет отправителя не найден
    @Test
    void optimisticTransferMoney_ShouldThrowException_WhenSenderAccountNotFound() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        when(accountRepository.findByUserId(senderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.optimisticTransferMoney(senderId, receiverId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Счет отправителя не найден");
    }

    // Негативный сценарий: Счет получателя не найден
    @Test
    void optimisticTransferMoney_ShouldThrowException_WhenReceiverAccountNotFound() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        Account senderAccount = mock(Account.class);

        when(accountRepository.findByUserId(senderId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(receiverId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.optimisticTransferMoney(senderId, receiverId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Счет получателя не найден");
    }

    // Негативный сценарий: Недостаточно средств на счете отправителя
    @Test
    void optimisticTransferMoney_ShouldThrowException_WhenInsufficientFunds() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        Account senderAccount = mock(Account.class);
        Account receiverAccount = mock(Account.class);

        when(accountRepository.findByUserId(senderId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(receiverId)).thenReturn(Optional.of(receiverAccount));
        when(senderAccount.withdraw(amount)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> transactionService.optimisticTransferMoney(senderId, receiverId, amount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Недостаточно средств на счете отправителя");
    }

    // Негативный сценарий: Исключение оптимистичной блокировки при сохранении аккаунтов
    @Test
    void optimisticTransferMoney_ShouldThrowOptimisticLockException_WhenRepositorySaveFails() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        Account senderAccount = mock(Account.class);
        Account receiverAccount = mock(Account.class);

        when(accountRepository.findByUserId(senderId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(receiverId)).thenReturn(Optional.of(receiverAccount));
        when(senderAccount.withdraw(amount)).thenReturn(true);
        // Имитация успешного депозита (void-метод)
        doNothing().when(receiverAccount).deposit(amount);

        // Симулируем исключение при сохранении отправителя
        OptimisticLockException lockException = new OptimisticLockException("Lock exception");
        when(accountRepository.save(senderAccount)).thenThrow(lockException);

        // Act & Assert
        assertThatThrownBy(() -> transactionService.optimisticTransferMoney(senderId, receiverId, amount))
                .isInstanceOf(OptimisticLockException.class)
                .hasMessageContaining("Ошибка оптимистичной блокировки");
    }

    // Позитивный сценарий: успешный перевод (optimistic)
    @Test
    void optimisticTransferMoney_ShouldTransferMoneySuccessfully() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        Account senderAccount = mock(Account.class);
        Account receiverAccount = mock(Account.class);

        when(accountRepository.findByUserId(senderId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(receiverId)).thenReturn(Optional.of(receiverAccount));
        when(senderAccount.withdraw(amount)).thenReturn(true);
        // Имитация успешного депозита (void-метод)
        doNothing().when(receiverAccount).deposit(amount);
        // Имитация успешного сохранения аккаунтов
        when(accountRepository.save(senderAccount)).thenReturn(senderAccount);
        when(accountRepository.save(receiverAccount)).thenReturn(receiverAccount);

        // Act
        transactionService.optimisticTransferMoney(senderId, receiverId, amount);

        // Assert: проверка вызовов методов
        verify(senderAccount).withdraw(amount);
        verify(receiverAccount).deposit(amount);
        verify(accountRepository).save(senderAccount);
        verify(accountRepository).save(receiverAccount);
    }

    // ===================== Тесты для pessimisticTransferMoney =====================

    // Негативный сценарий: сумма перевода null
    @Test
    void pessimisticTransferMoney_ShouldThrowException_WhenAmountIsNull() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = null;

        // Act & Assert
        assertThatThrownBy(() -> transactionService.pessimisticTransferMoney(senderId, receiverId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сумма перевода должна быть положительной");
    }

    // Негативный сценарий: сумма перевода меньше или равна нулю
    @Test
    void pessimisticTransferMoney_ShouldThrowException_WhenAmountIsZeroOrNegative() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal zeroAmount = BigDecimal.ZERO;
        BigDecimal negativeAmount = BigDecimal.valueOf(-5);

        // Act & Assert for zero
        assertThatThrownBy(() -> transactionService.pessimisticTransferMoney(senderId, receiverId, zeroAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сумма перевода должна быть положительной");

        // Act & Assert for negative value
        assertThatThrownBy(() -> transactionService.pessimisticTransferMoney(senderId, receiverId, negativeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сумма перевода должна быть положительной");
    }

    // Негативный сценарий: Первый аккаунт не найден
    @Test
    void pessimisticTransferMoney_ShouldThrowException_WhenFirstAccountNotFound() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;

        // Определяем первый идентификатор для блокировки (наименьший из senderId и receiverId)
        UUID firstLockId = senderId.compareTo(receiverId) <= 0 ? senderId : receiverId;
        when(accountRepository.findByUserIdForUpdate(firstLockId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.pessimisticTransferMoney(senderId, receiverId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Счет для пользователя " + firstLockId + " не найден");
    }

    // Негативный сценарий: Второй аккаунт не найден
    @Test
    void pessimisticTransferMoney_ShouldThrowException_WhenSecondAccountNotFound() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        UUID firstLockId = senderId.compareTo(receiverId) <= 0 ? senderId : receiverId;
        UUID secondLockId = senderId.compareTo(receiverId) <= 0 ? receiverId : senderId;

        Account firstAccount = mock(Account.class);
        when(accountRepository.findByUserIdForUpdate(firstLockId)).thenReturn(Optional.of(firstAccount));
        when(accountRepository.findByUserIdForUpdate(secondLockId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.pessimisticTransferMoney(senderId, receiverId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Счет для пользователя " + secondLockId + " не найден");
    }

    // Негативный сценарий: Недостаточно средств на счете отправителя (pessimistic)
    @Test
    void pessimisticTransferMoney_ShouldThrowException_WhenInsufficientFunds() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        UUID firstLockId = senderId.compareTo(receiverId) <= 0 ? senderId : receiverId;
        UUID secondLockId = senderId.compareTo(receiverId) <= 0 ? receiverId : senderId;

        Account firstAccount = mock(Account.class);
        Account secondAccount = mock(Account.class);
        when(accountRepository.findByUserIdForUpdate(firstLockId)).thenReturn(Optional.of(firstAccount));
        when(accountRepository.findByUserIdForUpdate(secondLockId)).thenReturn(Optional.of(secondAccount));

        // Определяем, какой аккаунт является отправителем, и имитируем недостаток средств
        Account senderAccount = senderId.equals(firstLockId) ? firstAccount : secondAccount;
        when(senderAccount.withdraw(amount)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> transactionService.pessimisticTransferMoney(senderId, receiverId, amount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Недостаточно средств на счете отправителя");
    }

    // Позитивный сценарий: успешный перевод (pessimistic)
    @Test
    void pessimisticTransferMoney_ShouldTransferMoneySuccessfully() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        UUID firstLockId = senderId.compareTo(receiverId) <= 0 ? senderId : receiverId;
        UUID secondLockId = senderId.compareTo(receiverId) <= 0 ? receiverId : senderId;

        Account firstAccount = mock(Account.class);
        Account secondAccount = mock(Account.class);
        when(accountRepository.findByUserIdForUpdate(firstLockId)).thenReturn(Optional.of(firstAccount));
        when(accountRepository.findByUserIdForUpdate(secondLockId)).thenReturn(Optional.of(secondAccount));

        // Определяем отправителя и получателя на основе порядка блокировки
        Account senderAccount = senderId.equals(firstLockId) ? firstAccount : secondAccount;
        Account receiverAccount = senderId.equals(firstLockId) ? secondAccount : firstAccount;
        when(senderAccount.withdraw(amount)).thenReturn(true);
        doNothing().when(receiverAccount).deposit(amount);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        transactionService.pessimisticTransferMoney(senderId, receiverId, amount);

        // Assert: проверяем, что средства сняты и зачислены, а аккаунты сохранены
        verify(senderAccount).withdraw(amount);
        verify(receiverAccount).deposit(amount);
        verify(accountRepository).save(senderAccount);
        verify(accountRepository).save(receiverAccount);
    }
}