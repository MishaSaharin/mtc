package com.MishaSaharin.mtc.exeptions;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("Could not find user " + id);
    }
}
