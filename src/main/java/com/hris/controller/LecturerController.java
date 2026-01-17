package com.hris.controller;

import com.hris.model.Department;
import com.hris.model.Employee;
import com.hris.model.LecturerProfile;
import com.hris.model.enums.LecturerEmploymentStatus;
import com.hris.model.enums.LecturerRank;
import com.hris.model.enums.LecturerWorkStatus;
import com.hris.service.DepartmentService;
import com.hris.service.EmployeeService;
import com.hris.service.LecturerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Lecturer Controller
 * Handles lecturer management pages and API endpoints
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/lecturers")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class LecturerController {

    private final LecturerService lecturerService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    // =====================================================
    // PAGE CONTROLLERS
    // =====================================================

    /**
     * Lecturer list page
     */
    @GetMapping
    public String listLecturers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LecturerRank rank,
            @RequestParam(required = false) LecturerEmploymentStatus empStatus,
            @RequestParam(required = false) LecturerWorkStatus workStatus,
            @RequestParam(required = false) Long prodiId,
            Model model) {
        log.info("Loading lecturer list page - search: {}, rank: {}, empStatus: {}, workStatus: {}, prodiId: {}",
                search, rank, empStatus, workStatus, prodiId);

        // Get filtered lecturers
        List<LecturerProfile> lecturers = lecturerService.searchLecturers(search, rank, empStatus, workStatus, prodiId);

        // Get all prodis for filter dropdown (departments with is_prodi = true)
        List<Department> prodis = departmentService.getAllProdis();

        // Stats
        long totalCount = lecturerService.countActiveLecturers();
        long permanentCount = lecturerService.countByEmploymentStatus(LecturerEmploymentStatus.DOSEN_TETAP);
        long activeCount = lecturerService.countByWorkStatus(LecturerWorkStatus.ACTIVE);

        model.addAttribute("activePage", "lecturers");
        model.addAttribute("lecturers", lecturers);
        model.addAttribute("prodis", prodis);

        // Filter parameters
        model.addAttribute("search", search);
        model.addAttribute("rank", rank);
        model.addAttribute("empStatus", empStatus);
        model.addAttribute("workStatus", workStatus);
        model.addAttribute("prodiId", prodiId);
        model.addAttribute("rankOptions", LecturerRank.values());
        model.addAttribute("empStatusOptions", LecturerEmploymentStatus.values());
        model.addAttribute("workStatusOptions", LecturerWorkStatus.values());

        // Stats
        model.addAttribute("totalLecturers", (int) totalCount);
        model.addAttribute("permanentCount", (int) permanentCount);
        model.addAttribute("activeCount", (int) activeCount);

        return "lecturer/list";
    }

    /**
     * Lecturer create form page
     */
    @GetMapping("/create")
    public String createLecturerForm(Model model) {
        log.info("Loading lecturer create form");

        model.addAttribute("activePage", "lecturers");
        model.addAttribute("lecturerProfile", new LecturerProfile());
        model.addAttribute("isEdit", false);

        // Add dropdown options
        addDropdownOptions(model);

        return "lecturer/form";
    }

    /**
     * Lecturer edit form page
     */
    @GetMapping("/{id}/edit")
    public String editLecturerForm(@PathVariable Long id, Model model) {
        log.info("Loading lecturer edit form for ID: {}", id);

        LecturerProfile lecturerProfile = lecturerService.getLecturerById(id);
        if (lecturerProfile == null) {
            return "redirect:/lecturers";
        }

        model.addAttribute("activePage", "lecturers");
        model.addAttribute("lecturerProfile", lecturerProfile);
        model.addAttribute("isEdit", true);

        // Add dropdown options
        addDropdownOptions(model);

        return "lecturer/form";
    }

    /**
     * Lecturer detail page
     */
    @GetMapping("/{id}")
    public String viewLecturerDetail(@PathVariable Long id, Model model) {
        log.info("Loading lecturer detail for ID: {}", id);

        LecturerProfile lecturerProfile = lecturerService.getLecturerById(id);
        if (lecturerProfile == null) {
            return "redirect:/lecturers";
        }

        model.addAttribute("activePage", "lecturers");
        model.addAttribute("lecturer", lecturerProfile);

        return "lecturer/detail";
    }

    // =====================================================
    // FORM SUBMISSION HANDLERS
    // =====================================================

    /**
     * Create new lecturer profile
     */
    @PostMapping("/create")
    public String createLecturer(@Valid @ModelAttribute LecturerProfile lecturerProfile,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        log.info("Creating lecturer profile for employee ID: {}", lecturerProfile.getEmployee() != null ? lecturerProfile.getEmployee().getId() : "null");

        // Validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("lecturerProfile", lecturerProfile);
            model.addAttribute("isEdit", false);
            addDropdownOptions(model);
            return "lecturer/form";
        }

        try {
            LecturerProfile saved = lecturerService.createLecturerProfile(lecturerProfile);
            redirectAttributes.addFlashAttribute("success", "Data dosen berhasil disimpan");
            return "redirect:/lecturers";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lecturerProfile", lecturerProfile);
            model.addAttribute("isEdit", false);
            addDropdownOptions(model);
            return "lecturer/form";
        }
    }

    /**
     * Update existing lecturer profile
     */
    @PostMapping("/{id}/edit")
    public String updateLecturer(@PathVariable Long id,
                                @Valid @ModelAttribute LecturerProfile lecturerProfile,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        log.info("Updating lecturer profile ID: {}", id);

        // Validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("lecturerProfile", lecturerProfile);
            model.addAttribute("isEdit", true);
            addDropdownOptions(model);
            return "lecturer/form";
        }

        try {
            LecturerProfile updated = lecturerService.updateLecturerProfile(id, lecturerProfile);
            redirectAttributes.addFlashAttribute("success", "Data dosen berhasil diperbarui");
            return "redirect:/lecturers";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lecturerProfile", lecturerProfile);
            model.addAttribute("isEdit", true);
            addDropdownOptions(model);
            return "lecturer/form";
        }
    }

    /**
     * Delete lecturer profile
     */
    @PostMapping("/{id}/delete")
    public String deleteLecturer(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        log.info("Deleting lecturer profile ID: {}", id);

        try {
            lecturerService.deleteLecturerProfile(id);
            redirectAttributes.addFlashAttribute("success", "Data dosen berhasil dihapus");
        } catch (IllegalArgumentException e) {
            log.error("Error deleting lecturer: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/lecturers";
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Add dropdown options to model for form
     */
    private void addDropdownOptions(Model model) {
        model.addAttribute("allEmployees", employeeService.getAllEmployees());
        model.addAttribute("prodis", departmentService.getAllProdis());
        model.addAttribute("rankOptions", LecturerRank.values());
        model.addAttribute("empStatusOptions", LecturerEmploymentStatus.values());
        model.addAttribute("workStatusOptions", LecturerWorkStatus.values());
    }
}
