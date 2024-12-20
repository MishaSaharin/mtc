package com.MishaSaharin.mtc.exeptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public class UserNotFoundExceptionAdvice {
    @ResponseBody
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public String userNotFoundExceptionHandler(UserNotFoundException ex) {
        return ex.getMessage();
    }
}
