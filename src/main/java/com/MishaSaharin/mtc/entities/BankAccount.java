package com.MishaSaharin.mtc.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "bank_accounts")
public class BankAccount extends BasedEntity {
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
    @Column(name = "changed_at")
    private ZonedDateTime changedAt;
    @Column(name = "user")
    @OneToOne(mappedBy = "bank_account_id")
    private User user;

    public BankAccount() {
    }

    public BankAccount(BigDecimal amount, ZonedDateTime createdAt, ZonedDateTime changedAt, User user) {
        this.amount = amount;
        this.createdAt = createdAt;
        this.changedAt = changedAt;
        this.user = user;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(ZonedDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) return false;
        BankAccount that = (BankAccount) o;
        return Objects.equals(amount, that.amount) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(changedAt, that.changedAt) &&
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), amount, createdAt, changedAt, user);
    }

    @Override
    public String toString() {
        return "BankAccount{"
                + "amount=" + amount
                + ", createdAt=" + createdAt
                + ", changedAt=" + changedAt
                + ", user=" + user
                + '}';
    }

    @Override
    public UUID getId() {
        return super.getUuid();
    }
}