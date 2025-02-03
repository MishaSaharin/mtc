package com.saccharine.mtc.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2) // precision и scale для BigDecimal
    private BigDecimal balance = BigDecimal.ZERO; // Инициализация нулем

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Account() {
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(accountNumber, account.accountNumber)
                && Objects.equals(balance, account.balance)
                && Objects.equals(user, account.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber, balance, user);
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", user=" + user +
                '}';
    }
}