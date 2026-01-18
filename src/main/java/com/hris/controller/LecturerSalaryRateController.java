package com.hris.controller;

import com.hris.model.LecturerSalaryRate;
import com.hris.model.enums.LecturerRank;
import com.hris.service.LecturerSalaryRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/lecturer-salary-rates")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class LecturerSalaryRateController {

    @Autowired
    private LecturerSalaryRateService rateService;

    @GetMapping
    public String listRates(Model model) {
        List<LecturerSalaryRate> rates = rateService.getAllRates();
        model.addAttribute("rates", rates);
        model.addAttribute("hasAllRanks", rateService.hasAllRanksConfigured());
        model.addAttribute("activePage", "lecturer-salary-rates");
        return "lecturer-salary-rate/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("rate", new LecturerSalaryRate());
        model.addAttribute("academicRanks", LecturerRank.values());
        model.addAttribute("activePage", "lecturer-salary-rates");
        return "lecturer-salary-rate/form";
    }

    @PostMapping("/save")
    public String saveRate(@ModelAttribute LecturerSalaryRate rate, RedirectAttributes redirectAttributes) {
        try {
            rateService.createRate(rate);
            redirectAttributes.addFlashAttribute("success", "Tarif berhasil disimpan");
            return "redirect:/lecturer-salary-rates";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lecturer-salary-rates/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var rateOpt = rateService.getRateById(id);
        if (rateOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Tarif tidak ditemukan");
            return "redirect:/lecturer-salary-rates";
        }
        model.addAttribute("rate", rateOpt.get());
        model.addAttribute("academicRanks", LecturerRank.values());
        model.addAttribute("edit", true);
        model.addAttribute("activePage", "lecturer-salary-rates");
        return "lecturer-salary-rate/form";
    }

    @PostMapping("/update/{id}")
    public String updateRate(@PathVariable Long id, @ModelAttribute LecturerSalaryRate rate,
                             RedirectAttributes redirectAttributes) {
        try {
            rateService.updateRate(id, rate);
            redirectAttributes.addFlashAttribute("success", "Tarif berhasil diperbarui");
            return "redirect:/lecturer-salary-rates";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lecturer-salary-rates/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteRate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rateService.deleteRate(id);
            redirectAttributes.addFlashAttribute("success", "Tarif berhasil dihapus");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lecturer-salary-rates";
    }
}
