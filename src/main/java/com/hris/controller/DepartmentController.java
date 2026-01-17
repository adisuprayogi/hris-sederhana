package com.hris.controller;

import com.hris.model.Department;
import com.hris.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Department Controller
 * Handles department management pages and API endpoints
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/departments")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class DepartmentController {

    private final DepartmentService departmentService;

    // =====================================================
    // PAGE CONTROLLORS
    // =====================================================

    /**
     * Department list page (with pagination and filtering)
     */
    @GetMapping
    public String listDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Boolean isProdi,
            Model model) {
        log.info("Loading department list page - page: {}, size: {}, search: {}, parentId: {}, isProdi: {}",
                page, size, search, parentId, isProdi);

        // Create pageable with sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        // Get paginated and filtered departments
        Page<Department> departmentPage = departmentService.searchDepartments(search, parentId, isProdi, pageable);

        // Get all departments for filter dropdown
        List<Department> allDepartments = departmentService.getAllDepartments();
        List<com.hris.model.Department> tree = departmentService.getDepartmentTree();

        model.addAttribute("departmentPage", departmentPage);
        model.addAttribute("departments", departmentPage.getContent());
        model.addAttribute("allDepartments", allDepartments);
        model.addAttribute("rootDepartments", tree);

        // Filter parameters
        model.addAttribute("search", search);
        model.addAttribute("parentId", parentId);
        model.addAttribute("isProdi", isProdi);

        // Pagination info
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", departmentPage.getTotalPages());
        model.addAttribute("totalItems", departmentPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Stats
        model.addAttribute("totalDepartments", (int) departmentPage.getTotalElements());
        model.addAttribute("rootCount", (int) departmentPage.stream().filter(d -> d.getParent() == null).count());
        model.addAttribute("prodiCount", (int) departmentPage.stream().filter(Department::getIsProdi).count());

        return "department/list";
    }

    /**
     * Department create form page
     */
    @GetMapping("/create")
    public String createDepartmentForm(Model model) {
        log.info("Loading department create form");

        model.addAttribute("department", new Department());
        model.addAttribute("parentOptions", departmentService.getAllDepartments());
        model.addAttribute("isEdit", false);

        return "department/form";
    }

    /**
     * Department edit form page
     */
    @GetMapping("/{id}/edit")
    public String editDepartmentForm(@PathVariable Long id, Model model) {
        log.info("Loading department edit form for ID: {}", id);

        Department department = departmentService.getDepartmentById(id);
        if (department == null) {
            return "redirect:/departments";
        }

        model.addAttribute("department", department);
        model.addAttribute("parentOptions", getAvailableParentOptions(id));
        model.addAttribute("isEdit", true);
        model.addAttribute("parentChain", departmentService.getParentChain(id));

        return "department/form";
    }

    /**
     * Department detail page
     */
    @GetMapping("/{id}")
    public String viewDepartment(@PathVariable Long id, Model model) {
        log.info("Loading department detail for ID: {}", id);

        Department department = departmentService.getDepartmentWithHeadAndParent(id);
        if (department == null) {
            return "redirect:/departments";
        }

        // Get the base DTO
        DepartmentService.DepartmentDto dto = departmentService.toDto(department);

        // Get parent chain to safely set parent info
        List<Department> parentChain = departmentService.getParentChain(id);
        if (parentChain.size() > 1) {
            // The immediate parent is the second-to-last in the chain
            Department immediateParent = parentChain.get(parentChain.size() - 2);
            dto.setParentId(immediateParent.getId());
            dto.setParentName(immediateParent.getName());
            // Set level based on position in chain (excluding self)
            dto.setLevel(parentChain.size() - 1);
        } else {
            dto.setLevel(0); // Root department
        }

        // Set head info if available - now safe because we used getDepartmentWithHeadAndParent
        if (department.getHead() != null) {
            dto.setHeadId(department.getHead().getId());
            dto.setHeadName(department.getHead().getFullName());
        }

        model.addAttribute("department", dto);
        model.addAttribute("parentChain", parentChain);
        model.addAttribute("children", departmentService.getChildDepartments(id));

        return "department/detail";
    }

    // =====================================================
    // FORM SUBMISSION HANDLERS
    // =====================================================

    /**
     * Create new department
     */
    @PostMapping("/create")
    public String createDepartment(@Valid @ModelAttribute Department department,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        log.info("Creating department: {}", department.getName());

        // Validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("department", department);
            model.addAttribute("parentOptions", departmentService.getAllDepartments());
            model.addAttribute("isEdit", false);
            return "department/form";
        }

        try {
            Department saved = departmentService.createDepartment(department);
            redirectAttributes.addFlashAttribute("success", "Data berhasil disimpan");
            return "redirect:/departments";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("department", department);
            model.addAttribute("parentOptions", departmentService.getAllDepartments());
            model.addAttribute("isEdit", false);
            return "department/form";
        }
    }

    /**
     * Update existing department
     */
    @PostMapping("/{id}/edit")
    public String updateDepartment(@PathVariable Long id,
                                   @Valid @ModelAttribute Department department,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        log.info("Updating department ID: {}", id);

        // Validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("department", department);
            model.addAttribute("parentOptions", getAvailableParentOptions(id));
            model.addAttribute("isEdit", true);
            model.addAttribute("parentChain", departmentService.getParentChain(id));
            return "department/form";
        }

        try {
            Department updated = departmentService.updateDepartment(id, department);
            redirectAttributes.addFlashAttribute("success", "Data berhasil disimpan");
            return "redirect:/departments";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("department", department);
            model.addAttribute("parentOptions", getAvailableParentOptions(id));
            model.addAttribute("isEdit", true);
            model.addAttribute("parentChain", departmentService.getParentChain(id));
            return "department/form";
        }
    }

    /**
     * Delete department
     */
    @PostMapping("/{id}/delete")
    public String deleteDepartment(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        log.info("Deleting department ID: {}", id);

        try {
            departmentService.deleteDepartment(id);
            redirectAttributes.addFlashAttribute("success", "Data berhasil dihapus");
        } catch (IllegalArgumentException e) {
            log.error("Error deleting department: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/departments";
    }

    /**
     * Set department head
     */
    @PostMapping("/{id}/set-head")
    public String setDepartmentHead(@PathVariable Long id,
                                     @RequestParam Long headId,
                                     RedirectAttributes redirectAttributes) {
        log.info("Setting department head - Dept ID: {}, Head ID: {}", id, headId);

        try {
            departmentService.setDepartmentHead(id, headId);
            redirectAttributes.addFlashAttribute("success", "Department head updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/departments/" + id;
    }

    // =====================================================
    // API ENDPOINTS (for AJAX/HTMX)
    // =====================================================

    /**
     * Get child departments by parent ID (API)
     */
    @GetMapping("/api/children/{parentId}")
    @ResponseBody
    public List<DepartmentService.DepartmentDto> getChildDepartments(@PathVariable Long parentId) {
        return departmentService.getChildDepartments(parentId).stream()
                .map(departmentService::toDto)
                .toList();
    }

    /**
     * Get department tree (API)
     */
    @GetMapping("/api/tree")
    @ResponseBody
    public List<DepartmentService.DepartmentDto> getDepartmentTree() {
        return departmentService.getDepartmentTreeAsDto();
    }

    /**
     * Get parent chain for department (API)
     */
    @GetMapping("/api/{id}/parent-chain")
    @ResponseBody
    public List<DepartmentService.DepartmentDto> getParentChain(@PathVariable Long id) {
        return departmentService.getParentChain(id).stream()
                .map(departmentService::toDto)
                .toList();
    }

    /**
     * Check if department can be deleted (API)
     */
    @GetMapping("/api/{id}/can-delete")
    @ResponseBody
    public boolean canDelete(@PathVariable Long id) {
        return !departmentService.hasChildren(id) && !departmentService.hasEmployees(id);
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Get available parent options (exclude self and descendants to prevent circular reference)
     */
    private List<Department> getAvailableParentOptions(Long currentId) {
        List<Department> allDepartments = departmentService.getAllDepartments();
        Department current = departmentService.getDepartmentById(currentId);

        // Filter out current department and its descendants
        return allDepartments.stream()
                .filter(dept -> !dept.getId().equals(currentId))
                .filter(dept -> !isDescendant(dept, current))
                .toList();
    }

    /**
     * Check if potentialChild is a descendant of potentialParent
     */
    private boolean isDescendant(Department potentialChild, Department potentialParent) {
        Department current = potentialChild;
        while (current.getParent() != null) {
            if (current.getParent().getId().equals(potentialParent.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
