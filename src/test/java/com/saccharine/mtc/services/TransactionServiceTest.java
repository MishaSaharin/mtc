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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
        assertThatThrownBy(() -> transactionService.optimisticTransferMoney(senderId, receiverId, null))
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

    @Test
    public void testConcurrentOptimisticTransfer() throws Exception {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        // Создаем счета с начальными балансами: отправитель 100, получатель 50
        Account senderAccount = new Account();
        senderAccount.setId(senderId);
        senderAccount.deposit(new BigDecimal("100.00"));

        Account receiverAccount = new Account();
        receiverAccount.setId(receiverId);
        receiverAccount.deposit(new BigDecimal("50.00"));

        // Мокаем методы поиска по идентификатору, возвращая подготовленные счета
        when(accountRepository.findByUserId(senderId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(receiverId)).thenReturn(Optional.of(receiverAccount));

        // Для вызова метода save(receiverAccount) будем возвращать переданный объект без изменений
        when(accountRepository.save(argThat(account -> account.equals(receiverAccount))))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Arrange
        // Суммы переводов
        BigDecimal transferAmount1 = new BigDecimal("30.00");
        BigDecimal transferAmount2 = new BigDecimal("40.00");

        // AtomicInteger для подсчета количества вызовов сохранения счета отправителя.
        // При первом вызове возвращаем счет, при втором выбрасываем исключение.
        AtomicInteger saveCounter = new AtomicInteger(0);
        when(accountRepository.save(argThat(account -> account.equals(senderAccount))))
                .thenAnswer(invocation -> {
                    int count = saveCounter.incrementAndGet();
                    if (count == 1) {
                        return invocation.getArgument(0);
                    } else {
                        throw new OptimisticLockException("Ошибка оптимистичной блокировки. Попробуйте повторить операцию позже.");
                    }
                });

        // Используем ExecutorService для параллельного выполнения двух переводов
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Boolean> task1 = () -> {
            try {
                transactionService.optimisticTransferMoney(senderId, receiverId, transferAmount1);
                return true; // перевод выполнен успешно
            } catch (OptimisticLockException ex) {
                return false; // перевод завершился ошибкой оптимистичной блокировки
            }
        };

        Callable<Boolean> task2 = () -> {
            try {
                transactionService.optimisticTransferMoney(senderId, receiverId, transferAmount2);
                return true;
            } catch (OptimisticLockException ex) {
                return false;
            }
        };

        // Act: запускаем задачи параллельно
        Future<Boolean> future1 = executor.submit(task1);
        Future<Boolean> future2 = executor.submit(task2);

        boolean result1 = future1.get();
        boolean result2 = future2.get();

        // Assert
        // Ожидаем, что ровно одна транзакция завершилась успешно
        int successCount = (result1 ? 1 : 0) + (result2 ? 1 : 0);
        assertThat(successCount).isEqualTo(1);

        // Если успешен перевод с суммой 30, то баланс отправителя будет 70, а получателя – 80
        // Если успешен перевод с суммой 40, то баланс отправителя будет 60, а получателя – 90
        if (result1) {
            assertThat(senderAccount.getBalance()).isEqualByComparingTo(new BigDecimal("70.00"));
            assertThat(receiverAccount.getBalance()).isEqualByComparingTo(new BigDecimal("80.00"));
        } else if (result2) {
            assertThat(senderAccount.getBalance()).isEqualByComparingTo(new BigDecimal("60.00"));
            assertThat(receiverAccount.getBalance()).isEqualByComparingTo(new BigDecimal("90.00"));
        }

        executor.shutdown();
    }
}