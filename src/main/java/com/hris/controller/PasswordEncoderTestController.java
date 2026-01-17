package com.hris.controller;

import com.hris.model.Employee;
import com.hris.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/encoder-test")
@RequiredArgsConstructor
public class PasswordEncoderTestController {

    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/verify")
    public Map<String, Object> verifyPassword(@RequestParam String email, @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        
        Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);
        
        if (employee == null) {
            result.put("error", "User not found");
            return result;
        }
        
        String dbHash = employee.getPassword();
        boolean matches = passwordEncoder.matches(password, dbHash);
        
        result.put("email", email);
        result.put("password_tested", password);
        result.put("db_hash", dbHash);
        result.put("matches", matches);
        result.put("encoder_type", passwordEncoder.getClass().getSimpleName());
        
        // Generate new hash for comparison
        String newHash = passwordEncoder.encode(password);
        result.put("new_hash", newHash);
        result.put("new_hash_matches_db", passwordEncoder.matches(password, newHash));
        
        return result;
    }
}
