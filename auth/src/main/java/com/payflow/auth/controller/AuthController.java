package com.payflow.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/health")
    public String health() {
        return "Auth module is working!";
    }

    @PostMapping("/signup")
    public String signup() {
        return "Signup endpoint working!";
    }
}
