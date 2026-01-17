package com.hris.controller;

import com.hris.model.Company;
import com.hris.model.enums.CompanyType;
import com.hris.model.enums.PayrollPeriodType;
import com.hris.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Company Controller
 * Handles company profile management (ADMIN only)
 * Note: Company is a singleton - only one active record should exist
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/company")
@PreAuthorize("hasRole('ADMIN')")
public class CompanyController {

    private final CompanyService companyService;

    // =====================================================
    // PAGE CONTROLLERS
    // =====================================================

    /**
     * Redirect /profile to / (backward compatibility)
     */
    @GetMapping("/profile")
    public String redirectToCompany() {
        return "redirect:/company";
    }

    /**
     * Company profile page (view only)
     */
    @GetMapping
    public String viewCompanyProfile(Model model) {
        log.info("Loading company profile page");

        Company company = companyService.getCompany();
        if (company == null) {
            // If no company exists, redirect to create page
            return "redirect:/company/create";
        }

        model.addAttribute("activePage", "company");
        model.addAttribute("company", company);
        model.addAttribute("isView", true);

        return "company/profile";
    }

    /**
     * Create company page (if no company exists yet)
     */
    @GetMapping("/create")
    public String createCompanyForm(Model model) {
        log.info("Loading company create form");

        // Check if company already exists
        if (companyService.companyExists()) {
            return "redirect:/company";
        }

        model.addAttribute("activePage", "company");
        model.addAttribute("company", new Company());
        model.addAttribute("isEdit", false);
        model.addAttribute("companyTypes", CompanyType.values());
        model.addAttribute("payrollPeriodTypes", PayrollPeriodType.values());

        return "company/form";
    }

    /**
     * Edit company page
     */
    @GetMapping("/edit")
    public String editCompanyForm(Model model) {
        log.info("Loading company edit form");

        Company company = companyService.getCompany();
        if (company == null) {
            return "redirect:/company/create";
        }

        model.addAttribute("activePage", "company");
        model.addAttribute("company", company);
        model.addAttribute("isEdit", true);
        model.addAttribute("companyTypes", CompanyType.values());
        model.addAttribute("payrollPeriodTypes", PayrollPeriodType.values());

        return "company/form";
    }

    // =====================================================
    // FORM SUBMISSION HANDLERS
    // =====================================================

    /**
     * Create new company
     */
    @PostMapping("/create")
    public String createCompany(@Valid @ModelAttribute Company company,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        log.info("Creating company: {}", company.getName());

        // Validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("company", company);
            model.addAttribute("isEdit", false);
            model.addAttribute("companyTypes", CompanyType.values());
            model.addAttribute("payrollPeriodTypes", PayrollPeriodType.values());
            return "company/form";
        }

        try {
            Company saved = companyService.saveCompany(company);
            redirectAttributes.addFlashAttribute("success", "Data perusahaan berhasil disimpan");
            return "redirect:/company";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("company", company);
            model.addAttribute("isEdit", false);
            model.addAttribute("companyTypes", CompanyType.values());
            return "company/form";
        }
    }

    /**
     * Update existing company
     */
    @PostMapping("/edit")
    public String updateCompany(@Valid @ModelAttribute Company company,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        log.info("Updating company ID: {}", company.getId());

        // Validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("company", company);
            model.addAttribute("isEdit", true);
            model.addAttribute("companyTypes", CompanyType.values());
            return "company/form";
        }

        try {
            // Get existing company to get the ID
            Company existing = companyService.getCompany();
            if (existing == null) {
                redirectAttributes.addFlashAttribute("error", "Data perusahaan tidak ditemukan");
                return "redirect:/company";
            }

            Company updated = companyService.updateCompany(existing.getId(), company);
            redirectAttributes.addFlashAttribute("success", "Data perusahaan berhasil diperbarui");
            return "redirect:/company";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("company", company);
            model.addAttribute("isEdit", true);
            model.addAttribute("companyTypes", CompanyType.values());
            return "company/form";
        }
    }

    // =====================================================
    // FILE UPLOAD HANDLERS
    // =====================================================

    /**
     * Upload company logo
     */
    @PostMapping("/logo")
    public String uploadLogo(@RequestParam("logo") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        log.info("Uploading company logo");

        Company company = companyService.getCompany();
        if (company == null) {
            redirectAttributes.addFlashAttribute("error", "Data perusahaan tidak ditemukan");
            return "redirect:/company";
        }

        try {
            String logoPath = companyService.uploadLogo(company.getId(), file);
            redirectAttributes.addFlashAttribute("success", "Logo berhasil diupload");
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error uploading logo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/company/edit";
    }

    /**
     * Delete company logo
     */
    @PostMapping("/logo/delete")
    public String deleteLogo(RedirectAttributes redirectAttributes) {
        log.info("Deleting company logo");

        Company company = companyService.getCompany();
        if (company == null) {
            redirectAttributes.addFlashAttribute("error", "Data perusahaan tidak ditemukan");
            return "redirect:/company";
        }

        try {
            companyService.deleteLogo(company.getId());
            redirectAttributes.addFlashAttribute("success", "Logo berhasil dihapus");
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error deleting logo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/company/edit";
    }

    /**
     * Upload company stamp
     */
    @PostMapping("/stamp")
    public String uploadStamp(@RequestParam("stamp") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        log.info("Uploading company stamp");

        Company company = companyService.getCompany();
        if (company == null) {
            redirectAttributes.addFlashAttribute("error", "Data perusahaan tidak ditemukan");
            return "redirect:/company";
        }

        try {
            String stampPath = companyService.uploadStamp(company.getId(), file);
            redirectAttributes.addFlashAttribute("success", "Stempel berhasil diupload");
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error uploading stamp: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/company/edit";
    }

    /**
     * Delete company stamp
     */
    @PostMapping("/stamp/delete")
    public String deleteStamp(RedirectAttributes redirectAttributes) {
        log.info("Deleting company stamp");

        Company company = companyService.getCompany();
        if (company == null) {
            redirectAttributes.addFlashAttribute("error", "Data perusahaan tidak ditemukan");
            return "redirect:/company";
        }

        try {
            companyService.deleteStamp(company.getId());
            redirectAttributes.addFlashAttribute("success", "Stempel berhasil dihapus");
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error deleting stamp: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/company/edit";
    }
}
