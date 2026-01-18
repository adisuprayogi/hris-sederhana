package com.hris.controller;

import com.hris.model.WorkingHours;
import com.hris.service.WorkingHoursService;
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
 * Working Hours Controller
 * Master Jam Kerja - Layer 1 Shift System
 */
@Slf4j
@Controller
@RequestMapping("/working-hours")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@RequiredArgsConstructor
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;

    @GetMapping
    public String listWorkingHours(Model model) {
        List<WorkingHours> workingHours = workingHoursService.getAllWorkingHours();
        model.addAttribute("workingHours", workingHours);
        model.addAttribute("activePage", "working-hours");
        return "working-hours/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("workingHours", new WorkingHours());
        model.addAttribute("activePage", "working-hours");
        return "working-hours/form";
    }

    @PostMapping("/save")
    public String saveWorkingHours(@Valid @ModelAttribute WorkingHours workingHours,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        log.info("Saving working hours: {}", workingHours.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("workingHours", workingHours);
            return "working-hours/form";
        }

        try {
            workingHoursService.createWorkingHours(workingHours);
            redirectAttributes.addFlashAttribute("success", "Working hours berhasil disimpan");
            return "redirect:/working-hours";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("workingHours", workingHours);
            return "working-hours/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        WorkingHours workingHours = workingHoursService.getWorkingHoursById(id);
        if (workingHours == null) {
            redirectAttributes.addFlashAttribute("error", "Working hours tidak ditemukan");
            return "redirect:/working-hours";
        }

        model.addAttribute("workingHours", workingHours);
        model.addAttribute("edit", true);
        model.addAttribute("activePage", "working-hours");
        return "working-hours/form";
    }

    @PostMapping("/update/{id}")
    public String updateWorkingHours(@PathVariable Long id,
                                     @Valid @ModelAttribute WorkingHours workingHours,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        log.info("Updating working hours ID: {}", id);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("workingHours", workingHours);
            model.addAttribute("edit", true);
            return "working-hours/form";
        }

        try {
            workingHoursService.updateWorkingHours(id, workingHours);
            redirectAttributes.addFlashAttribute("success", "Working hours berhasil diperbarui");
            return "redirect:/working-hours";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("workingHours", workingHours);
            model.addAttribute("edit", true);
            return "working-hours/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteWorkingHours(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            workingHoursService.deleteWorkingHours(id);
            redirectAttributes.addFlashAttribute("success", "Working hours berhasil dihapus");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/working-hours";
    }
}
