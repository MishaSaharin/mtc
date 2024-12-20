package com.MishaSaharin.mtc.exeptions;

import java.math.BigDecimal;
import java.util.UUID;

public class WrongAmountException extends RuntimeException {

    public WrongAmountException(BigDecimal amount) {
        super("Wrong amount exception. " + "Amount: " + amount);
    }

    public WrongAmountException(UUID id, BigDecimal amount) {
        super("Wrong amount exception. Bank account id: " + id + " Amount: " + amount);
    }
}