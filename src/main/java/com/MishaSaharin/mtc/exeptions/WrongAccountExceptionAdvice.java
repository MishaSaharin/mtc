package com.MishaSaharin.mtc.exeptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class WrongAccountExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(WrongAccountException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public String wrongAccountExceptionHandler(WrongAccountException ex) {
        return ex.getMessage();
    }
}