package com.techcorp.employee.controller;

import com.techcorp.employee.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/files")
public class FilesViewController {

    private final EmployeeService employeeService;

    public FilesViewController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public String index(Model model) {
        // provide company list for report generation dropdown
        var stats = employeeService.getCompanyStatistics();
        List<String> companies = stats == null ? List.of() : stats.keySet().stream().sorted().toList();
        model.addAttribute("companies", companies);
        return "files/index";
    }
}
