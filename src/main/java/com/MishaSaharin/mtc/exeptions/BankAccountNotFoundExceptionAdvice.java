package com.MishaSaharin.mtc.exeptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public class BankAccountNotFoundExceptionAdvice {
    @ResponseBody
    @ExceptionHandler(BankAccountNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String bankAccountNotFoundExceptionHandler(BankAccountNotFoundException ex) {
        return ex.getMessage();
    }
}