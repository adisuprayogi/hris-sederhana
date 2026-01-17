package com.hris.controller;

import com.hris.model.Employee;
import com.hris.model.enums.RoleType;
import com.hris.repository.EmployeeRepository;
import com.hris.repository.EmployeeRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

/**
 * Authentication Controller
 * Handles login, logout, and role selection
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeRoleRepository employeeRoleRepository;

    /**
     * Display login page
     */
    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    /**
     * Display role selection page
     * Shown when user has multiple roles
     */
    @GetMapping("/auth/select-role")
    public String selectRolePage(HttpServletRequest request, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/auth/login";
        }

        // Get user email from authentication
        String email = authentication.getName();
        Employee user = employeeRepository.findByEmailAndDeletedAtIsNull(email)
                .orElse(null);

        if (user == null) {
            return "redirect:/auth/login";
        }

        // Get user roles
        Set<RoleType> roles = employeeRoleRepository.findRolesByEmployeeId(user.getId());

        if (roles.isEmpty()) {
            // User has no roles, this shouldn't happen
            log.error("User {} has no roles assigned", email);
            return "redirect:/auth/logout";
        }

        if (roles.size() == 1) {
            // User has only one role, auto-select it
            RoleType singleRole = roles.iterator().next();
            return "redirect:/auth/confirm-role?role=" + singleRole.name();
        }

        // User has multiple roles, show selection page
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("selectedRole", request.getSession().getAttribute("selectedRole"));

        return "auth/role-selection";
    }

    /**
     * Process role selection
     */
    @PostMapping("/auth/select-role")
    public String selectRole(
            @RequestParam("role") String role,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            RoleType selectedRole = RoleType.valueOf(role);
            HttpSession session = request.getSession(true);
            session.setAttribute("selectedRole", selectedRole.name());

            log.info("User selected role: {}", selectedRole);

            // Redirect to appropriate dashboard based on role
            return switch (selectedRole) {
                case ADMIN -> "redirect:/dashboard/admin";
                case HR -> "redirect:/dashboard/hr";
                case EMPLOYEE -> "redirect:/dashboard/employee";
                case DOSEN -> "redirect:/dashboard/dosen";
            };
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid role selected");
            return "redirect:/auth/select-role";
        }
    }

    /**
     * Process role selection via GET (for links)
     */
    @GetMapping("/auth/confirm-role")
    public String confirmRole(
            @RequestParam(value = "role", required = false) String role,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (role == null || role.isEmpty()) {
            return "redirect:/auth/select-role";
        }

        return selectRole(role, request, redirectAttributes);
    }

    /**
     * Handle after successful login
     * Spring Security will redirect here by default
     */
    @GetMapping("/login-success")
    public String loginSuccess(HttpServletRequest request) {
        return "redirect:/auth/select-role";
    }

    /**
     * Handle login failure
     */
    @GetMapping("/auth/login-error")
    public String loginError(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Invalid email or password");
        return "redirect:/auth/login";
    }
}
