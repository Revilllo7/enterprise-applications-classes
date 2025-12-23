package com.techcorp.employee.controller;

import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.service.DepartmentService;
import com.techcorp.employee.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/statistics")
public class StatisticsViewController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public StatisticsViewController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String index(Model model) {
        List<Employee> all = employeeService.getAllEmployees();

        int totalEmployees = all.size();
        double avgSalary = all.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
        int totalDepartments = departmentService.getAllDepartments().size();

        Map<String, CompanyStatistics> byCompany = employeeService.getCompanyStatistics();

        // distribution by position (name -> count)
        Map<String, Long> byPosition = all.stream()
                .map(e -> e.getPosition() == null ? "UNKNOWN" : e.getPosition().name())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("avgSalary", avgSalary);
        model.addAttribute("totalDepartments", totalDepartments);
        model.addAttribute("companyStats", byCompany);
        model.addAttribute("byPosition", byPosition);
        return "statistics/index";
    }

    @GetMapping("/company/{name}")
    public String companyDetails(@PathVariable("name") String name, Model model) {
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
        CompanyStatistics cs = stats.getOrDefault(name, new CompanyStatistics(0,0.0,""));
        // collect employees for company
        List<Employee> employees = employeeService.getAllEmployees().stream()
                .filter(e -> e.getCompanyName() != null && e.getCompanyName().equalsIgnoreCase(name))
                .collect(Collectors.toList());

        model.addAttribute("companyName", name);
        model.addAttribute("stats", cs);
        model.addAttribute("employees", employees);
        return "statistics/company";
    }
}
