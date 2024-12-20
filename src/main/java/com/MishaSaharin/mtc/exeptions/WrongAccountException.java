package com.MishaSaharin.mtc.exeptions;

import java.util.UUID;

public class WrongAccountException extends RuntimeException {

    public WrongAccountException(UUID id) {
        super("The same account address for from and to exception. Bank account id: " + id);
    }
}