package com.hris.controller;

import com.hris.model.ShiftPackage;
import com.hris.model.WorkingHours;
import com.hris.service.ShiftPackageService;
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
 * Shift Package Controller
 * Paket Shift (Kombinasi Working Hours per Hari) - Layer 2 Shift System
 */
@Slf4j
@Controller
@RequestMapping("/shift-packages")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@RequiredArgsConstructor
public class ShiftPackageController {

    private final ShiftPackageService shiftPackageService;
    private final WorkingHoursService workingHoursService;

    @GetMapping
    public String listShiftPackages(Model model) {
        List<ShiftPackage> shiftPackages = shiftPackageService.getAllShiftPackagesWithWorkingHours();
        model.addAttribute("shiftPackages", shiftPackages);
        model.addAttribute("activePage", "shift-packages");
        return "shift-packages/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("shiftPackage", new ShiftPackage());
        model.addAttribute("workingHoursList", workingHoursService.getAllWorkingHours());
        model.addAttribute("activePage", "shift-packages");
        return "shift-packages/form";
    }

    @PostMapping("/save")
    public String saveShiftPackage(@Valid @ModelAttribute ShiftPackage shiftPackage,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        log.info("Saving shift package: {}", shiftPackage.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("shiftPackage", shiftPackage);
            model.addAttribute("workingHoursList", workingHoursService.getAllWorkingHours());
            return "shift-packages/form";
        }

        try {
            shiftPackageService.createShiftPackage(shiftPackage);
            redirectAttributes.addFlashAttribute("success", "Shift package berhasil disimpan");
            return "redirect:/shift-packages";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("shiftPackage", shiftPackage);
            model.addAttribute("workingHoursList", workingHoursService.getAllWorkingHours());
            return "shift-packages/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        ShiftPackage shiftPackage = shiftPackageService.getShiftPackageByIdWithWorkingHours(id);
        if (shiftPackage == null) {
            redirectAttributes.addFlashAttribute("error", "Shift package tidak ditemukan");
            return "redirect:/shift-packages";
        }

        model.addAttribute("shiftPackage", shiftPackage);
        model.addAttribute("workingHoursList", workingHoursService.getAllWorkingHours());
        model.addAttribute("edit", true);
        model.addAttribute("activePage", "shift-packages");
        return "shift-packages/form";
    }

    @PostMapping("/update/{id}")
    public String updateShiftPackage(@PathVariable Long id,
                                     @Valid @ModelAttribute ShiftPackage shiftPackage,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        log.info("Updating shift package ID: {}", id);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("shiftPackage", shiftPackage);
            model.addAttribute("workingHoursList", workingHoursService.getAllWorkingHours());
            model.addAttribute("edit", true);
            return "shift-packages/form";
        }

        try {
            shiftPackageService.updateShiftPackage(id, shiftPackage);
            redirectAttributes.addFlashAttribute("success", "Shift package berhasil diperbarui");
            return "redirect:/shift-packages";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("shiftPackage", shiftPackage);
            model.addAttribute("workingHoursList", workingHoursService.getAllWorkingHours());
            model.addAttribute("edit", true);
            return "shift-packages/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteShiftPackage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            shiftPackageService.deleteShiftPackage(id);
            redirectAttributes.addFlashAttribute("success", "Shift package berhasil dihapus");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/shift-packages";
    }
}
