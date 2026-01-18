package com.hris.controller;

import com.hris.model.LecturerSalary;
import com.hris.model.LecturerProfile;
import com.hris.model.enums.LecturerSalaryStatus;
import com.hris.model.enums.LecturerEmploymentStatus;
import com.hris.service.LecturerProfileService;
import com.hris.service.LecturerSalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/lecturer-salaries")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class LecturerSalaryController {

    @Autowired
    private LecturerSalaryService salaryService;

    @Autowired
    private LecturerProfileService lecturerProfileService;

    @GetMapping
    public String listSalaries(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) LecturerEmploymentStatus lecturerType,
            Model model) {
        List<LecturerSalary> salaries;

        if (period != null && lecturerType != null) {
            salaries = salaryService.getSalariesByPeriodAndLecturerEmploymentStatus(period, lecturerType);
        } else if (period != null) {
            salaries = salaryService.getSalariesByPeriod(period);
        } else {
            salaries = salaryService.getAllSalaries();
        }

        model.addAttribute("salaries", salaries);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("selectedLecturerType", lecturerType);
        model.addAttribute("lecturerTypes", LecturerEmploymentStatus.values());
        model.addAttribute("activePage", "lecturer-salaries");
        return "lecturer-salary/list";
    }

    @GetMapping("/calculate")
    public String showCalculateForm(Model model) {
        List<LecturerProfile> lecturers = lecturerProfileService.getAllActiveProfiles();
        model.addAttribute("lecturers", lecturers);
        model.addAttribute("lecturerTypes", LecturerEmploymentStatus.values());
        model.addAttribute("activePage", "lecturer-salaries");
        return "lecturer-salary/calculate";
    }

    @PostMapping("/calculate")
    public String calculateSalary(
            @RequestParam Long lecturerProfileId,
            @RequestParam String period,
            RedirectAttributes redirectAttributes) {
        try {
            LecturerProfile profile = lecturerProfileService.getById(lecturerProfileId);
            if (profile == null) {
                redirectAttributes.addFlashAttribute("error", "Dosen tidak ditemukan");
                return "redirect:/lecturer-salaries/calculate";
            }

            LecturerSalary salary;
            if (profile.getEmploymentStatus() == LecturerEmploymentStatus.DOSEN_TETAP) {
                salary = salaryService.calculatePermanentLecturerSalary(lecturerProfileId, period);
            } else {
                salary = salaryService.calculateContractLecturerSalary(lecturerProfileId, period);
            }

            redirectAttributes.addFlashAttribute("success", "Gaji dosen berhasil dihitung");
            return "redirect:/lecturer-salaries/view/" + salary.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lecturer-salaries/calculate";
        }
    }

    @GetMapping("/view/{id}")
    public String viewSalary(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        LecturerSalary salary = salaryService.getSalaryById(id);
        if (salary == null) {
            redirectAttributes.addFlashAttribute("error", "Data gaji tidak ditemukan");
            return "redirect:/lecturer-salaries";
        }

        LecturerProfile profile = lecturerProfileService.getById(salary.getLecturerProfileId());

        model.addAttribute("salary", salary);
        model.addAttribute("lecturer", profile);
        model.addAttribute("activePage", "lecturer-salaries");
        return "lecturer-salary/view";
    }

    @GetMapping("/lecturer/{lecturerId}")
    public String viewLecturerSalaries(@PathVariable Long lecturerId, Model model) {
        List<LecturerSalary> salaries = salaryService.getSalariesByLecturer(lecturerId);
        LecturerProfile profile = lecturerProfileService.getById(lecturerId);

        model.addAttribute("salaries", salaries);
        model.addAttribute("lecturer", profile);
        return "lecturer-salary/lecturer-history";
    }

    @PostMapping("/mark-paid/{id}")
    public String markAsPaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            salaryService.markAsPaid(id);
            redirectAttributes.addFlashAttribute("success", "Gaji berhasil ditandai sebagai dibayar");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lecturer-salaries/view/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteSalary(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            salaryService.deleteSalary(id);
            redirectAttributes.addFlashAttribute("success", "Data gaji berhasil dihapus");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lecturer-salaries";
    }
}
