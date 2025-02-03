//package com.saccharine.mtc.controllers;
//
//import com.saccharine.mtc.exeptions.AccountNotFoundException;
//import com.saccharine.mtc.exeptions.InsufficientFundsException;
//import com.saccharine.mtc.exeptions.UserAlreadyExistsException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestControllerAdvice("mctGlobalExceptionHandler")
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getFieldErrors().forEach(error ->
//                errors.put(error.getField(), error.getDefaultMessage()));
//        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(UserAlreadyExistsException.class)
//    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
//    }
//
//    @ExceptionHandler(InsufficientFundsException.class)
//    public ResponseEntity<String> handleInsufficientFundsException(InsufficientFundsException ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(AccountNotFoundException.class)
//    public ResponseEntity<String> handleAccountNotFoundException(AccountNotFoundException ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleGenericException(Exception ex) {
//        return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//}