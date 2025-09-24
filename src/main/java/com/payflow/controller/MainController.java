package com.payflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MainController {
    
    @GetMapping("/health")
    public String health() {
        return "PayFlow main application is running!";
    }
}
