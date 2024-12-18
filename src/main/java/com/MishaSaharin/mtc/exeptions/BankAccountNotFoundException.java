package com.MishaSaharin.mtc.exeptions;

import java.util.UUID;

public class BankAccountNotFoundException extends RuntimeException {
    public BankAccountNotFoundException(UUID uuid) {
        super("Could not found bank account " + uuid);
    }
}
