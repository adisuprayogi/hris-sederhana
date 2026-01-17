package com.hris.controller;

import com.hris.model.Employee;
import com.hris.model.EmployeeJobHistory;
import com.hris.service.EmployeeJobHistoryService;
import com.hris.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Job History Controller
 * Handles employee job history viewing
 */
@Slf4j
@Controller
@RequestMapping("/employees/{employeeId}/job-history")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@RequiredArgsConstructor
public class JobHistoryController {

    private final EmployeeJobHistoryService jobHistoryService;
    private final EmployeeService employeeService;

    /**
     * Job history list page for an employee
     */
    @GetMapping
    @Transactional(readOnly = true)
    public String viewJobHistory(@PathVariable Long employeeId, Model model) {
        log.info("Loading job history for employee ID: {}", employeeId);

        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            return "redirect:/employees";
        }

        List<EmployeeJobHistory> jobHistory = jobHistoryService.getEmployeeJobHistory(employeeId);

        model.addAttribute("activePage", "employees");
        model.addAttribute("employee", employee);
        model.addAttribute("jobHistory", jobHistory);

        return "employee/job-history";
    }
}
