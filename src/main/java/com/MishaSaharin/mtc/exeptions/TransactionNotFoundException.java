package com.MishaSaharin.mtc.exeptions;

import java.util.UUID;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(UUID uuid) {
        super("Could not found transaction " + uuid);
    }
}
