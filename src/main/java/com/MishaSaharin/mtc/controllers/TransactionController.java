package com.MishaSaharin.mtc.controllers;

import com.MishaSaharin.mtc.services.TransactionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
