package com.cloudfold.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/health")
    public Map<String, String> health(){
         return Map.of("status", "ok", "db", "connected");
    }

    @GetMapping("/version")
    public Map<String, String> version(){
        return Map.of(
            "version", "1.0.0",
            "service", "cloudfold-control-plane"
        );
    }
}