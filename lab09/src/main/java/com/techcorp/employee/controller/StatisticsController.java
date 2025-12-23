package com.techcorp.employee.controller;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final EmployeeService employeeService;

    public StatisticsController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/salary/average")
    public ResponseEntity<Map<String, Double>> averageSalary(@RequestParam(value = "company", required = false) String company) {
        double avg;
        if (company == null || company.isBlank()) {
            avg = employeeService.getAllEmployees().stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
        } else {
            String cmp = company.trim();
            avg = employeeService.getAllEmployees().stream()
                    .filter(e -> cmp.equals(e.getCompanyName()))
                    .mapToDouble(Employee::getSalary)
                    .average().orElse(0.0);
        }
        return ResponseEntity.ok(Map.of("averageSalary", avg));
    }

    @GetMapping("/company/{companyName}")
    public ResponseEntity<CompanyStatisticsDTO> companyStatistics(@PathVariable("companyName") String companyName) {
        if (companyName == null) return ResponseEntity.notFound().build();
        String key = companyName.trim();

        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
        CompanyStatistics cs = stats.get(key);
        if (cs == null) {
            return ResponseEntity.notFound().build();
        }

        // compute highest salary for the company by scanning employees
        double highestSalary = employeeService.getAllEmployees().stream()
                .filter(e -> key.equals(e.getCompanyName()))
                .mapToDouble(Employee::getSalary)
                .max().orElse(0.0);

        CompanyStatisticsDTO dto = new CompanyStatisticsDTO();
        dto.setCompanyName(key);
        dto.setEmployeeCount(cs.getEmployeeCount());
        dto.setAverageSalary(cs.getAverageSalary());
        dto.setHighestSalary(highestSalary);
        dto.setTopEarnerName(cs.getHighestPaidFullName());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/positions")
    public ResponseEntity<Map<String, Integer>> positions() {
        Map<String, Integer> counts = employeeService.getAllEmployees().stream()
                .filter(e -> e.getPosition() != null)
                .collect(Collectors.groupingBy(e -> e.getPosition().name(), Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Integer>> statusDistribution() {
        Map<String, Integer> map = employeeService.getStatusDistribution().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().intValue()));
        return ResponseEntity.ok(map);
    }
}

