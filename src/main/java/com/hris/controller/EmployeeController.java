package com.hris.controller;

import com.hris.model.Department;
import com.hris.model.Employee;
import com.hris.model.Position;
import com.hris.model.enums.EmployeeStatus;
import com.hris.model.enums.EmploymentStatus;
import com.hris.service.DepartmentService;
import com.hris.service.EmployeeService;
import com.hris.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

/**
 * Employee Controller
 * Handles employee management pages and API endpoints
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/employees")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final PositionService positionService;

    // =====================================================
    // PAGE CONTROLLERS
    // =====================================================

    /**
     * Employee list page (with pagination and filtering)
     */
    @GetMapping
    @Transactional(readOnly = true)
    public String listEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmploymentStatus employmentStatus,
            Model model) {
        log.info("Loading employee list page - page: {}, size: {}, search: {}, status: {}, departmentId: {}, employmentStatus: {}",
                page, size, search, status, departmentId, employmentStatus);

        // Create pageable with sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());

        // Get paginated and filtered employees
        Page<Employee> employeePage = employeeService.searchEmployees(search, status, departmentId, employmentStatus, pageable);

        // Get all departments for filter dropdown
        List<Department> departments = departmentService.getAllDepartments();

        model.addAttribute("activePage", "employees");
        model.addAttribute("employeePage", employeePage);
        model.addAttribute("employees", employeePage.getContent());
        model.addAttribute("departments", departments);

        // Filter parameters
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("employmentStatus", employmentStatus);
        model.addAttribute("statusOptions", EmployeeStatus.values());
        model.addAttribute("employmentStatuses", EmploymentStatus.values());

        // Pagination info
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());
        model.addAttribute("totalItems", employeePage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Stats
        long activeCount = employeeService.countByStatus(EmployeeStatus.ACTIVE);
        long inactiveCount = employeeService.countByStatus(EmployeeStatus.INACTIVE);
        long resignedCount = employeeService.countByStatus(EmployeeStatus.RESIGNED);

        model.addAttribute("totalEmployees", (int) employeePage.getTotalElements());
        model.addAttribute("activeCount", (int) activeCount);
        model.addAttribute("inactiveCount", (int) inactiveCount);
        model.addAttribute("resignedCount", (int) resignedCount);

        return "employee/list";
    }

    /**
     * Employee create form page
     */
    @GetMapping("/create")
    public String createEmployeeForm(Model model) {
        log.info("Loading employee create form");

        model.addAttribute("activePage", "employees");
        model.addAttribute("employee", new Employee());
        model.addAttribute("isEdit", false);

        // Add dropdown options
        addDropdownOptions(model);

        return "employee/form";
    }

    /**
     * Employee edit form page
     */
    @GetMapping("/{id}/edit")
    @Transactional(readOnly = true)
    public String editEmployeeForm(@PathVariable Long id, Model model) {
        log.info("Loading employee edit form for ID: {}", id);

        Employee employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            return "redirect:/employees";
        }

        model.addAttribute("activePage", "employees");
        model.addAttribute("employee", employee);
        model.addAttribute("isEdit", true);

        // Add dropdown options
        addDropdownOptions(model);

        return "employee/form";
    }

    /**
     * Employee detail page
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String viewEmployeeDetail(@PathVariable Long id, Model model) {
        log.info("Loading employee detail for ID: {}", id);

        Employee employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            return "redirect:/employees";
        }

        model.addAttribute("activePage", "employees");
        model.addAttribute("employee", employee);

        return "employee/detail";
    }

    // =====================================================
    // FORM SUBMISSION HANDLERS
    // =====================================================

    /**
     * Create new employee
     */
    @PostMapping("/create")
    public String createEmployee(@Valid @ModelAttribute Employee employee,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        log.info("Creating employee: {} ({})", employee.getFullName(), employee.getNik());

        // Validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("employee", employee);
            model.addAttribute("isEdit", false);
            addDropdownOptions(model);
            return "employee/form";
        }

        try {
            Employee saved = employeeService.createEmployee(employee);
            redirectAttributes.addFlashAttribute("success", "Data berhasil disimpan");
            return "redirect:/employees";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("employee", employee);
            model.addAttribute("isEdit", false);
            addDropdownOptions(model);
            return "employee/form";
        }
    }

    /**
     * Update existing employee
     */
    @PostMapping("/{id}/edit")
    public String updateEmployee(@PathVariable Long id,
                                @Valid @ModelAttribute Employee employee,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        log.info("Updating employee ID: {}", id);

        // Validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("employee", employee);
            model.addAttribute("isEdit", true);
            addDropdownOptions(model);
            return "employee/form";
        }

        try {
            Employee updated = employeeService.updateEmployee(id, employee);
            redirectAttributes.addFlashAttribute("success", "Data berhasil disimpan");
            return "redirect:/employees";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("employee", employee);
            model.addAttribute("isEdit", true);
            addDropdownOptions(model);
            return "employee/form";
        }
    }

    /**
     * Delete employee
     */
    @PostMapping("/{id}/delete")
    public String deleteEmployee(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        log.info("Deleting employee ID: {}", id);

        try {
            employeeService.deleteEmployee(id);
            redirectAttributes.addFlashAttribute("success", "Data berhasil dihapus");
        } catch (IllegalArgumentException e) {
            log.error("Error deleting employee: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/employees";
    }

    /**
     * Upload employee photo
     */
    @PostMapping("/{id}/photo")
    public String uploadPhoto(@PathVariable Long id,
                             @RequestParam("photo") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        log.info("Uploading photo for employee ID: {}", id);

        try {
            String photoPath = employeeService.uploadPhoto(id, file);
            redirectAttributes.addFlashAttribute("success", "Foto berhasil diupload");
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error uploading photo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/employees/" + id + "/edit";
    }

    /**
     * Delete employee photo
     */
    @PostMapping("/{id}/photo/delete")
    public String deletePhoto(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        log.info("Deleting photo for employee ID: {}", id);

        try {
            employeeService.deletePhoto(id);
            redirectAttributes.addFlashAttribute("success", "Foto berhasil dihapus");
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error deleting photo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/employees/" + id + "/edit";
    }

    /**
     * Set approver for employee
     */
    @PostMapping("/{id}/approver")
    public String setApprover(@PathVariable Long id,
                             @RequestParam Long approverId,
                             RedirectAttributes redirectAttributes) {
        log.info("Setting approver for employee ID: {} -> approver ID: {}", id, approverId);

        try {
            employeeService.setApprover(id, approverId);
            redirectAttributes.addFlashAttribute("success", "Approver berhasil diatur");
        } catch (IllegalArgumentException e) {
            log.error("Error setting approver: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/employees/" + id + "/edit";
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Add dropdown options to model for form
     */
    private void addDropdownOptions(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("positions", positionService.getAllPositions());
        model.addAttribute("allEmployees", employeeService.getAllEmployees());
        model.addAttribute("statusOptions", EmployeeStatus.values());
    }
}
