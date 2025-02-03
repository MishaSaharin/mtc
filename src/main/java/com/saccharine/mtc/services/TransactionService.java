package com.saccharine.mtc.services;

import com.saccharine.mtc.entities.Account;
//import com.saccharine.mtc.exeptions.AccountNotFoundException;
//import com.saccharine.mtc.exeptions.InsufficientFundsException;
import com.saccharine.mtc.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;

    @Autowired
    public TransactionService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transferMoney(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + fromAccountNumber));
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + toAccountNumber));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds in account: " + fromAccountNumber);
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount)); // Вычитаем сумму
        toAccount.setBalance(toAccount.getBalance().add(amount)); // Добавляем сумму

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }
}