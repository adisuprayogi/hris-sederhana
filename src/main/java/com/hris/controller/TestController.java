package com.hris.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/encode")
    public Map<String, String> encodePassword(@RequestParam String password) {
        Map<String, String> result = new HashMap<>();
        String encoded = passwordEncoder.encode(password);
        result.put("password", password);
        result.put("encoded", encoded);
        return result;
    }

    @GetMapping("/match")
    public Map<String, Object> matchPassword(
            @RequestParam String password,
            @RequestParam String encoded) {
        Map<String, Object> result = new HashMap<>();
        boolean matches = passwordEncoder.matches(password, encoded);
        result.put("password", password);
        result.put("encoded", encoded);
        result.put("matches", matches);
        return result;
    }

    @GetMapping("/verify-users")
    public Map<String, Object> verifyUsers() {
        Map<String, Object> result = new HashMap<>();

        // Verify admin123
        String adminHash = "$2a$10$dXJ3SW6G7P50lGmMkkmweN20dDAHoTBqY8fZ5Tv9Q5j2JzGW3MYQy";
        boolean adminMatches = passwordEncoder.matches("admin123", adminHash);

        // Verify test123
        String testHash = "$2a$10$5RMJxPyfwoUo6.9tG8/L0eqP1GU3H3UvqKKlCWU9q8RK8GpLY2p1u";
        boolean testMatches = passwordEncoder.matches("test123", testHash);

        result.put("admin123 matches hash", adminMatches);
        result.put("test123 matches hash", testMatches);

        // Generate new hashes
        result.put("new hash for admin123", passwordEncoder.encode("admin123"));
        result.put("new hash for test123", passwordEncoder.encode("test123"));

        return result;
    }
}
