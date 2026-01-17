package com.hris.controller;

import com.hris.model.Employee;
import com.hris.model.SalaryHistory;
import com.hris.service.EmployeeService;
import com.hris.service.SalaryHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * Salary History Controller
 * Handles employee salary history viewing
 */
@Slf4j
@Controller
@RequestMapping("/employees/{employeeId}/salary-history")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@RequiredArgsConstructor
public class SalaryHistoryController {

    private final SalaryHistoryService salaryHistoryService;
    private final EmployeeService employeeService;

    /**
     * Salary history list page for an employee
     */
    @GetMapping
    @Transactional(readOnly = true)
    public String viewSalaryHistory(@PathVariable Long employeeId, Model model) {
        log.info("Loading salary history for employee ID: {}", employeeId);

        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            return "redirect:/employees";
        }

        List<SalaryHistory> salaryHistory = salaryHistoryService.getEmployeeSalaryHistory(employeeId);

        // Calculate statistics
        BigDecimal totalIncrease = salaryHistoryService.getTotalSalaryIncrease(employeeId);
        SalaryHistory latestSalary = salaryHistoryService.getLatestSalary(employeeId);

        model.addAttribute("activePage", "employees");
        model.addAttribute("employee", employee);
        model.addAttribute("salaryHistory", salaryHistory);
        model.addAttribute("totalIncrease", totalIncrease);
        model.addAttribute("latestSalary", latestSalary);

        return "employee/salary-history";
    }
}
