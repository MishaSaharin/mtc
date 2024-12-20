package com.MishaSaharin.mtc.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "bank_accounts")
public class BankAccount extends BasedEntity {
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "user")
    @OneToOne(mappedBy = "bank_account_id")
    private User user;

    public BankAccount() {
    }

    public BankAccount(BigDecimal amount, User user) {
        this.amount = amount;
        this.user = user;
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
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), amount, user);
    }

    @Override
    public String toString() {
        return "BankAccount{"
                + "amount=" + amount
                + ", user=" + user
                + '}';
    }

    @Override
    public UUID getId() {
        return super.getUuid();
    }
}