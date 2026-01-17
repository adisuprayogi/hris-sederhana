package com.hris.config;

import com.hris.model.Employee;
import com.hris.model.enums.RoleType;
import com.hris.repository.EmployeeRepository;
import com.hris.repository.EmployeeRoleRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global Model Attributes
 * Adds common attributes to all models across all controllers
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final EmployeeRepository employeeRepository;
    private final EmployeeRoleRepository employeeRoleRepository;

    /**
     * Add selectedRole to all models
     */
    @ModelAttribute
    public void addAttributes(Model model, HttpSession session) {
        // Add selected role from session
        String selectedRole = (String) session.getAttribute("selectedRole");
        if (selectedRole != null) {
            model.addAttribute("selectedRole", selectedRole);
        }

        // Add authenticated user if not already present
        if (!model.containsAttribute("user")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

                String email = authentication.getName();
                Employee user = employeeRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);

                if (user != null) {
                    Set<String> roles = employeeRoleRepository.findRolesByEmployeeId(user.getId())
                            .stream()
                            .map(Enum::name)
                            .collect(Collectors.toSet());

                    model.addAttribute("user", user);
                    model.addAttribute("userRoles", roles);
                }
            }
        }
    }
}
