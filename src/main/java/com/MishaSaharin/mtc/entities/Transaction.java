package com.MishaSaharin.mtc.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction extends BasedEntity {
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "user")
    @OneToOne(mappedBy = "user_id")
    private User user;
    @Column(name = "bank_account")
    @OneToOne(mappedBy = "bank_account_id")
    private BankAccount bankAccount;

    public Transaction() {
    }

    public Transaction(ZonedDateTime createdAt, BigDecimal amount, User user, BankAccount bankAccount) {
        this.createdAt = createdAt;
        this.amount = amount;
        this.user = user;
        this.bankAccount = bankAccount;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Transaction that = (Transaction) o;
        return Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(user, that.user) &&
                Objects.equals(bankAccount, that.bankAccount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), createdAt, amount, user, bankAccount);
    }

    @Override
    public String toString() {
        return "Transaction{"
                + "createdAt=" + createdAt
                + ", amount=" + amount
                + ", user=" + user
                + ", bankAccount=" + bankAccount
                + '}';
    }

    @Override
    public UUID getId() {
        return super.getUuid();
    }
}
