package com.techcorp.employee.service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;
import org.springframework.stereotype.Service;

import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;

/**
 * Serwis przechowujący pracowników i udostępniający operacje analityczne.
 * Wszystkie operacje na kolekcjach wykonane przez Stream API.
 */
@Service
public class EmployeeService {
    private final Map<String, Employee> employeesByEmail = new HashMap<>();

    public boolean addEmployee(Employee employee) {
        if (employee == null || employee.getEmail() == null || employee.getEmail().isBlank()) return false;
        String emailKey = employee.getEmail().toLowerCase(Locale.ROOT);
        if (employeesByEmail.containsKey(emailKey)) return false; // nie dodawaj duplikatu
        employeesByEmail.put(emailKey, employee);
        return true;
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeesByEmail.values());
    }

    public Optional<Employee> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return Optional.ofNullable(employeesByEmail.get(email.toLowerCase(Locale.ROOT)));
    }

    public List<Employee> findByStatus(com.techcorp.employee.model.EmploymentStatus status) {
        if (status == null) return List.of();
        return employeesByEmail.values().stream()
                .filter(e -> status.equals(e.getStatus()))
                .collect(Collectors.toList());
    }

    public boolean updateStatus(String email, com.techcorp.employee.model.EmploymentStatus status) {
        if (email == null || email.isBlank() || status == null) return false;
        String key = email.toLowerCase(Locale.ROOT);
        Employee existing = employeesByEmail.get(key);
        if (existing == null) return false;
        existing.setStatus(status);
        return true;
    }

    public java.util.Map<com.techcorp.employee.model.EmploymentStatus, Long> getStatusDistribution() {
        return employeesByEmail.values().stream()
                .collect(Collectors.groupingBy(Employee::getStatus, Collectors.counting()));
    }

    public boolean removeEmployee(String email) {
        if (email == null || email.isBlank()) return false;
        String key = email.toLowerCase(Locale.ROOT);
        return employeesByEmail.remove(key) != null;
    }

    public Optional<Employee> updateEmployee(String email, Employee updated) {
        if (email == null || email.isBlank() || updated == null) return Optional.empty();
        String key = email.toLowerCase(Locale.ROOT);
        Employee existing = employeesByEmail.get(key);
        if (existing == null) return Optional.empty();

        // create a new Employee instance preserving the identity (email)
        String newFullName = updated.getFullName() == null ? existing.getFullName() : updated.getFullName();
        String newCompany = updated.getCompanyName();
        Position newPosition = updated.getPosition();
        double newSalary = updated.getSalary();

        Employee replacement = new Employee(newFullName, existing.getEmail(), newCompany, newPosition, newSalary);
        employeesByEmail.put(key, replacement);
        return Optional.of(replacement);
    }



     // Zwraca listę pracowników, których wynagrodzenie jest niższe niż bazowe dla ich stanowiska.
    public List<Employee> validateSalaryConsistency() {
        return employeesByEmail.values().stream()
                .filter(employee -> {
                    Position position = employee.getPosition();
                    if (position == null) return false;
                    return employee.getSalary() < position.getSalary();
                })
                .sorted(Comparator.comparing(Employee::getFullName))
                .collect(Collectors.toList());
    }

    // tworzy mapę statystyk firmy (nazwa firmy -> statystyki)
    public Map<String, CompanyStatistics> getCompanyStatistics() {
        // grupowanie po nazwie firmy bez zmiany oryginalnego zapisu (trimowane)
        Map<String, List<Employee>> grouped = employeesByEmail.values().stream()
                .collect(Collectors.groupingBy(employee -> {
                    String company = employee.getCompanyName();
                    return (company == null) ? "unknown" : company.trim();
                }));

        return grouped.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<Employee> list = entry.getValue();
                            int count = list.size();
                            double avgSalary = list.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
                            String highest = list.stream()
                                    .max(Comparator.comparingDouble(Employee::getSalary))
                                    .map(Employee::getFullName)
                                    .orElse("");
                            return new CompanyStatistics(count, avgSalary, highest);
                        }
                ));
    }

    // raport w formacie "Imię Nazwisko (email): aktualne_wynagrodzenie < bazowe_wynagrodzenie"
    public List<String> salaryConsistencyReport() {
        return validateSalaryConsistency().stream()
                .map(employee -> String.format("%s (%s): %.2f < %.2f", employee.getFullName(), employee.getEmail(), employee.getSalary(), employee.getPosition().getSalary()))
                .collect(Collectors.toList());
    }
}
