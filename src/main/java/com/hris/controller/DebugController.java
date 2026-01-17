package com.hris.controller;

import com.hris.model.Employee;
import com.hris.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final EmployeeRepository employeeRepository;

    @GetMapping("/find-user")
    public Map<String, Object> findUser(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<Employee> employee = employeeRepository.findByEmailAndDeletedAtIsNull(email);
        
        result.put("email_searched", email);
        result.put("found", employee.isPresent());
        
        if (employee.isPresent()) {
            Employee e = employee.get();
            result.put("id", e.getId());
            result.put("email", e.getEmail());
            result.put("fullName", e.getFullName());
            result.put("status", e.getStatus());
            result.put("deletedAt", e.getDeletedAt());
            result.put("password_length", e.getPassword() != null ? e.getPassword().length() : 0);
            result.put("password_prefix", e.getPassword() != null ? e.getPassword().substring(0, Math.min(30, e.getPassword().length())) : null);
        } else {
            result.put("error", "User not found");
        }
        
        return result;
    }
}
