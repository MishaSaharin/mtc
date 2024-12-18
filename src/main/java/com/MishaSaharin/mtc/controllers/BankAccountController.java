package com.MishaSaharin.mtc.controllers;

import com.MishaSaharin.mtc.services.BankAccountService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BankAccountController {
    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }
}
