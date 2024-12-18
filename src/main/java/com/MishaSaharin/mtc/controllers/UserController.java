package com.MishaSaharin.mtc.controllers;

import com.MishaSaharin.mtc.services.UserService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
}
