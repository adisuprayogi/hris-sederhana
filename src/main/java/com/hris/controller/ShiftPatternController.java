package com.hris.controller;

import com.hris.model.ShiftPackage;
import com.hris.model.ShiftPattern;
import com.hris.model.enums.ShiftType;
import com.hris.service.ShiftPackageService;
import com.hris.service.ShiftPatternService;
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
 * Shift Pattern Controller
 * Pattern (Shift Package + Permissions) - Layer 3 Shift System
 */
@Slf4j
@Controller
@RequestMapping("/shift-patterns")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@RequiredArgsConstructor
public class ShiftPatternController {

    private final ShiftPatternService shiftPatternService;
    private final ShiftPackageService shiftPackageService;

    @GetMapping
    public String listShiftPatterns(Model model) {
        List<ShiftPattern> shiftPatterns = shiftPatternService.getAllShiftPatternsWithShiftPackage();
        model.addAttribute("shiftPatterns", shiftPatterns);
        model.addAttribute("activePage", "shift-patterns");
        return "shift-patterns/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("shiftPattern", new ShiftPattern());
        model.addAttribute("shiftPackages", shiftPackageService.getAllShiftPackagesWithWorkingHours());
        model.addAttribute("shiftTypes", ShiftType.values());
        model.addAttribute("activePage", "shift-patterns");
        return "shift-patterns/form";
    }

    @PostMapping("/save")
    public String saveShiftPattern(@Valid @ModelAttribute ShiftPattern shiftPattern,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        log.info("Saving shift pattern: {}", shiftPattern.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("shiftPattern", shiftPattern);
            model.addAttribute("shiftPackages", shiftPackageService.getAllShiftPackagesWithWorkingHours());
            model.addAttribute("shiftTypes", ShiftType.values());
            return "shift-patterns/form";
        }

        try {
            shiftPatternService.createShiftPattern(shiftPattern);
            redirectAttributes.addFlashAttribute("success", "Shift pattern berhasil disimpan");
            return "redirect:/shift-patterns";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("shiftPattern", shiftPattern);
            model.addAttribute("shiftPackages", shiftPackageService.getAllShiftPackagesWithWorkingHours());
            model.addAttribute("shiftTypes", ShiftType.values());
            return "shift-patterns/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        ShiftPattern shiftPattern = shiftPatternService.getShiftPatternByIdWithShiftPackage(id);
        if (shiftPattern == null) {
            redirectAttributes.addFlashAttribute("error", "Shift pattern tidak ditemukan");
            return "redirect:/shift-patterns";
        }

        model.addAttribute("shiftPattern", shiftPattern);
        model.addAttribute("shiftPackages", shiftPackageService.getAllShiftPackagesWithWorkingHours());
        model.addAttribute("shiftTypes", ShiftType.values());
        model.addAttribute("edit", true);
        model.addAttribute("activePage", "shift-patterns");
        return "shift-patterns/form";
    }

    @PostMapping("/update/{id}")
    public String updateShiftPattern(@PathVariable Long id,
                                     @Valid @ModelAttribute ShiftPattern shiftPattern,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        log.info("Updating shift pattern ID: {}", id);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("shiftPattern", shiftPattern);
            model.addAttribute("shiftPackages", shiftPackageService.getAllShiftPackagesWithWorkingHours());
            model.addAttribute("shiftTypes", ShiftType.values());
            model.addAttribute("edit", true);
            return "shift-patterns/form";
        }

        try {
            shiftPatternService.updateShiftPattern(id, shiftPattern);
            redirectAttributes.addFlashAttribute("success", "Shift pattern berhasil diperbarui");
            return "redirect:/shift-patterns";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("shiftPattern", shiftPattern);
            model.addAttribute("shiftPackages", shiftPackageService.getAllShiftPackagesWithWorkingHours());
            model.addAttribute("shiftTypes", ShiftType.values());
            model.addAttribute("edit", true);
            return "shift-patterns/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteShiftPattern(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            shiftPatternService.deleteShiftPattern(id);
            redirectAttributes.addFlashAttribute("success", "Shift pattern berhasil dihapus");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/shift-patterns";
    }
}
