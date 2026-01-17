package com.hris.controller;

import com.hris.model.Employee;
import com.hris.repository.EmployeeRepository;
import com.hris.repository.EmployeeRoleRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dashboard Controller
 * Handles dashboard routing based on user role
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeeRepository employeeRepository;
    private final EmployeeRoleRepository employeeRoleRepository;

    /**
     * Admin Dashboard
     */
    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        // Check if user has selected ADMIN role
        String selectedRole = (String) session.getAttribute("selectedRole");
        if (!"ADMIN".equals(selectedRole)) {
            return "redirect:/auth/select-role";
        }

        // Add user info to model
        addUserToModel(session, model);

        // Add statistics for admin
        model.addAttribute("totalEmployees", employeeRepository.findAllActive().size());
        model.addAttribute("totalDepartments", 7); // Placeholder
        model.addAttribute("totalPositions", 7); // Placeholder

        // Add active page indicator
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("title", "Admin Dashboard");

        return "dashboard/admin";
    }

    /**
     * HR Dashboard
     */
    @GetMapping("/hr")
    public String hrDashboard(HttpSession session, Model model) {
        // Check if user has selected HR role
        String selectedRole = (String) session.getAttribute("selectedRole");
        if (!"HR".equals(selectedRole) && !"ADMIN".equals(selectedRole)) {
            return "redirect:/auth/select-role";
        }

        // Add user info to model
        addUserToModel(session, model);

        // Add statistics for HR
        model.addAttribute("totalEmployees", employeeRepository.findAllActive().size());
        model.addAttribute("pendingLeaves", 0); // Placeholder - will be implemented

        // Add active page indicator
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("title", "HR Dashboard");

        return "dashboard/hr";
    }

    /**
     * Employee Dashboard
     */
    @GetMapping("/employee")
    public String employeeDashboard(HttpSession session, Model model) {
        // Check if user has selected EMPLOYEE role
        String selectedRole = (String) session.getAttribute("selectedRole");
        if (!"EMPLOYEE".equals(selectedRole) && !"ADMIN".equals(selectedRole) &&
            !"HR".equals(selectedRole)) {
            return "redirect:/auth/select-role";
        }

        // Add user info to model
        Employee user = addUserToModel(session, model);

        // Add employee specific data
        // Placeholder - will be implemented with attendance service
        model.addAttribute("todayAttendance", null);
        model.addAttribute("leaveBalance", null);

        // Add active page indicator
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("title", "Employee Dashboard");

        return "dashboard/employee";
    }

    /**
     * Lecturer Dashboard
     */
    @GetMapping("/dosen")
    public String dosenDashboard(HttpSession session, Model model) {
        // Check if user has selected DOSEN role
        String selectedRole = (String) session.getAttribute("selectedRole");
        if (!"DOSEN".equals(selectedRole) && !"ADMIN".equals(selectedRole) &&
            !"HR".equals(selectedRole)) {
            return "redirect:/auth/select-role";
        }

        // Add user info to model
        Employee user = addUserToModel(session, model);

        // Add lecturer specific data
        // Placeholder - will be implemented with lecturer service
        model.addAttribute("todayAttendance", null);
        model.addAttribute("leaveBalance", null);

        // Add active page indicator
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("title", "Lecturer Dashboard");

        return "dashboard/dosen";
    }

    /**
     * Default dashboard redirect based on selected role
     */
    @GetMapping
    public String dashboard(HttpSession session) {
        String selectedRole = (String) session.getAttribute("selectedRole");

        if (selectedRole == null) {
            return "redirect:/auth/select-role";
        }

        return switch (selectedRole) {
            case "ADMIN" -> "redirect:/dashboard/admin";
            case "HR" -> "redirect:/dashboard/hr";
            case "EMPLOYEE" -> "redirect:/dashboard/employee";
            case "DOSEN" -> "redirect:/dashboard/dosen";
            default -> "redirect:/auth/select-role";
        };
    }

    /**
     * Helper method to add user info to model
     */
    private Employee addUserToModel(HttpSession session, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        Employee user = employeeRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);

        if (user != null) {
            Set<String> roles = employeeRoleRepository.findRolesByEmployeeId(user.getId())
                    .stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());

            model.addAttribute("user", user);
            model.addAttribute("userRoles", roles);
            model.addAttribute("selectedRole", (String) session.getAttribute("selectedRole"));
        }

        return user;
    }
}
