package com.hris.controller;

import com.hris.model.Employee;
import com.hris.model.EmployeeRole;
import com.hris.model.enums.RoleType;
import com.hris.repository.EmployeeRepository;
import com.hris.repository.EmployeeRoleRepository;
import com.hris.service.EmployeeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User Management Controller
 * Handles user role management for Administrator
 */
@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsersController {

    private final EmployeeRepository employeeRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final EmployeeService employeeService;

    // =====================================================
    // PAGES
    // =====================================================

    /**
     * User management list page
     */
    @GetMapping
    public String usersList(Model model) {
        List<Employee> employees = employeeRepository.findAllActive();

        // Create DTO list with roles
        List<UserWithRolesDTO> usersWithRoles = employees.stream()
                .map(this::mapToUserWithRolesDTO)
                .sorted(Comparator.comparing(UserWithRolesDTO::getFullName))
                .collect(Collectors.toList());

        model.addAttribute("users", usersWithRoles);
        model.addAttribute("allRoles", RoleType.values());
        model.addAttribute("activePage", "users");
        return "users/list";
    }

    /**
     * Search users
     */
    @GetMapping("/search")
    public String searchUsers(@RequestParam(required = false) String search, Model model) {
        List<Employee> employees;

        if (search == null || search.trim().isEmpty()) {
            employees = employeeRepository.findAllActive();
        } else {
            employees = employeeRepository.searchByNameOrEmail(search.trim());
        }

        List<UserWithRolesDTO> usersWithRoles = employees.stream()
                .map(this::mapToUserWithRolesDTO)
                .sorted(Comparator.comparing(UserWithRolesDTO::getFullName))
                .collect(Collectors.toList());

        model.addAttribute("users", usersWithRoles);
        model.addAttribute("allRoles", RoleType.values());
        model.addAttribute("search", search);
        model.addAttribute("activePage", "users");
        return "users/list";
    }

    // =====================================================
    // ACTIONS (AJAX)
    // =====================================================

    /**
     * Assign role to user (AJAX)
     */
    @PostMapping("/{employeeId}/role")
    @ResponseBody
    public Map<String, Object> assignRole(
            @PathVariable Long employeeId,
            @RequestParam String role,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();

        try {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

            if (employee.getDeletedAt() != null) {
                response.put("success", false);
                response.put("message", "Employee tidak aktif");
                return response;
            }

            RoleType roleType = RoleType.valueOf(role);

            // Check if role already exists
            if (employeeRoleRepository.existsByEmployeeIdAndRole(employeeId, roleType)) {
                response.put("success", false);
                response.put("message", "Role sudah ada");
                return response;
            }

            // Create new employee role
            EmployeeRole employeeRole = new EmployeeRole();
            employeeRole.setEmployeeId(employeeId);
            employeeRole.setRole(roleType);
            employeeRoleRepository.save(employeeRole);

            log.info("Role {} assigned to employee {} by {}", role, employeeId, principal.getName());

            response.put("success", true);
            response.put("message", "Role berhasil ditambahkan");
            response.put("role", role);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Role tidak valid: " + role);
        } catch (Exception e) {
            log.error("Error assigning role", e);
            response.put("success", false);
            response.put("message", "Terjadi kesalahan: " + e.getMessage());
        }

        return response;
    }

    /**
     * Remove role from user (AJAX)
     */
    @DeleteMapping("/{employeeId}/role/{role}")
    @ResponseBody
    public Map<String, Object> removeRole(
            @PathVariable Long employeeId,
            @PathVariable String role,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();

        try {
            RoleType roleType = RoleType.valueOf(role);

            // Find and soft delete the role
            EmployeeRole employeeRole = employeeRoleRepository
                    .findByEmployeeIdAndRoleAndDeletedAtIsNull(employeeId, roleType)
                    .orElseThrow(() -> new IllegalArgumentException("Role tidak ditemukan"));

            employeeRole.setDeletedAt(java.time.LocalDateTime.now());
            employeeRoleRepository.save(employeeRole);

            log.info("Role {} removed from employee {} by {}", role, employeeId, principal.getName());

            response.put("success", true);
            response.put("message", "Role berhasil dihapus");
            response.put("role", role);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            log.error("Error removing role", e);
            response.put("success", false);
            response.put("message", "Terjadi kesalahan: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get user details (for Edit Modal)
     */
    @GetMapping("/{employeeId}")
    @ResponseBody
    public Map<String, Object> getUserDetails(@PathVariable Long employeeId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Employee employee = employeeRepository.findEmployeeByIdWithRelationships(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

            Map<String, Object> userData = new HashMap<>();
            userData.put("employeeId", employee.getId());
            userData.put("fullName", employee.getFullName());
            userData.put("email", employee.getEmail());
            userData.put("nik", employee.getNik());
            userData.put("department", employee.getDepartment() != null ? employee.getDepartment().getName() : "-");
            userData.put("position", employee.getPosition() != null ? employee.getPosition().getName() : "-");
            userData.put("status", employee.getStatus() != null ? employee.getStatus().getDisplayName() : "-");
            userData.put("roles", getRolesForEmployee(employee.getId()));

            response.put("success", true);
            response.put("user", userData);
        } catch (Exception e) {
            log.error("Error fetching user details", e);
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * Update user roles (for Edit Modal)
     */
    @PutMapping("/{employeeId}/roles")
    @ResponseBody
    public Map<String, Object> updateUserRoles(
            @PathVariable Long employeeId,
            @RequestBody Map<String, List<String>> request,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();

        try {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

            if (employee.getDeletedAt() != null) {
                response.put("success", false);
                response.put("message", "Employee tidak aktif");
                return response;
            }

            List<String> newRoles = request.get("roles");
            if (newRoles == null || newRoles.isEmpty()) {
                response.put("success", false);
                response.put("message", "Minimal 1 role harus dipilih");
                return response;
            }

            // Get current roles
            Set<RoleType> currentRoles = employeeRoleRepository.findRolesByEmployeeId(employeeId);

            // Remove roles that are not in the new list
            for (RoleType currentRole : currentRoles) {
                if (!newRoles.contains(currentRole.name())) {
                    EmployeeRole employeeRole = employeeRoleRepository
                            .findByEmployeeIdAndRoleAndDeletedAtIsNull(employeeId, currentRole)
                            .orElse(null);
                    if (employeeRole != null) {
                        employeeRole.setDeletedAt(java.time.LocalDateTime.now());
                        employeeRoleRepository.save(employeeRole);
                    }
                }
            }

            // Add new roles
            for (String roleStr : newRoles) {
                RoleType roleType = RoleType.valueOf(roleStr);
                if (!currentRoles.contains(roleType)) {
                    EmployeeRole employeeRole = new EmployeeRole();
                    employeeRole.setEmployeeId(employeeId);
                    employeeRole.setRole(roleType);
                    employeeRoleRepository.save(employeeRole);
                }
            }

            log.info("Roles updated for employee {} by {}", employeeId, principal.getName());

            response.put("success", true);
            response.put("message", "Role berhasil diperbarui");
            response.put("roles", getRolesForEmployee(employee.getId()));
        } catch (Exception e) {
            log.error("Error updating roles", e);
            response.put("success", false);
            response.put("message", "Terjadi kesalahan: " + e.getMessage());
        }

        return response;
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private UserWithRolesDTO mapToUserWithRolesDTO(Employee employee) {
        UserWithRolesDTO dto = new UserWithRolesDTO();
        dto.setEmployeeId(employee.getId());
        dto.setFullName(employee.getFullName());
        dto.setEmail(employee.getEmail());
        dto.setNik(employee.getNik());
        dto.setDepartment(employee.getDepartment() != null ? employee.getDepartment().getName() : "-");
        dto.setPosition(employee.getPosition() != null ? employee.getPosition().getName() : "-");
        dto.setStatus(employee.getStatus() != null ? employee.getStatus().getDisplayName() : "-");
        dto.setRoles(getRolesForEmployee(employee.getId()));
        return dto;
    }

    private Set<String> getRolesForEmployee(Long employeeId) {
        Set<RoleType> roles = employeeRoleRepository.findRolesByEmployeeId(employeeId);
        return roles.stream()
                .map(RoleType::name)
                .collect(Collectors.toSet());
    }

    // =====================================================
    // DTO
    // =====================================================

    @Data
    public static class UserWithRolesDTO {
        private Long employeeId;
        private String fullName;
        private String email;
        private String nik;
        private String department;
        private String position;
        private String status;
        private Set<String> roles;
    }
}
