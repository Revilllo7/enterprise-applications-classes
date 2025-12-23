package com.techcorp.employee.service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Serwis przechowujący pracowników i udostępniający operacje analityczne.
 * Wszystkie operacje na kolekcjach wykonane przez Stream API.
 */
@Service
@Validated
public class EmployeeService {
    private final EmployeeRepository repository;
    public EmployeeService(EmployeeRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    public boolean addEmployee(@Valid @NotNull Employee employee) {
        if (employee == null || employee.getEmail() == null || employee.getEmail().isBlank()) return false;
        String emailKey = employee.getEmail().toLowerCase(Locale.ROOT);
        if (repository.existsByEmailIgnoreCase(emailKey)) return false;
        repository.save(employee);
        return true;
    }

    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    public Optional<Employee> findByEmail(@NotBlank String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return repository.findByEmailIgnoreCase(email.toLowerCase(Locale.ROOT));
    }

    public List<Employee> findByStatus(@NotNull EmploymentStatus status) {
        if (status == null) return List.of();
        return repository.findByStatus(status);
    }

    public boolean updateStatus(@NotBlank String email, @NotNull EmploymentStatus status) {
        if (email == null || email.isBlank() || status == null) return false;
        String key = email.toLowerCase(Locale.ROOT);
        Optional<Employee> existingOpt = repository.findByEmailIgnoreCase(key);
        if (existingOpt.isEmpty()) return false;
        Employee existing = existingOpt.get();
        existing.setStatus(status);
        repository.save(existing);
        return true;
    }

    public java.util.Map<com.techcorp.employee.model.EmploymentStatus, Long> getStatusDistribution() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(Employee::getStatus, Collectors.counting()));
    }

    public boolean removeEmployee(@NotBlank String email) {
        if (email == null || email.isBlank()) return false;
        String key = email.toLowerCase(Locale.ROOT);
        Optional<Employee> existing = repository.findByEmailIgnoreCase(key);
        if (existing.isEmpty()) return false;
        repository.deleteByEmailIgnoreCase(key);
        return true;
    }

    public Optional<Employee> updateEmployee(@NotBlank String email, @Valid @NotNull Employee updated) {
        if (email == null || email.isBlank() || updated == null) return Optional.empty();
        String key = email.toLowerCase(Locale.ROOT);
        Optional<Employee> existingOpt = repository.findByEmailIgnoreCase(key);
        if (existingOpt.isEmpty()) return Optional.empty();
        Employee existing = existingOpt.get();

        // create a new Employee instance preserving the identity (email)
        String newFullName = updated.getFullName() == null ? existing.getFullName() : updated.getFullName();
        String newCompany = updated.getCompanyName();
        Position newPosition = updated.getPosition();
        double newSalary = updated.getSalary();

        Employee replacement = new Employee(existing.getId(), newFullName, existing.getEmail(), newCompany, newPosition, newSalary);
        repository.save(replacement);
        return Optional.of(replacement);
    }



     // Zwraca listę pracowników, których wynagrodzenie jest niższe niż bazowe dla ich stanowiska.
    public List<Employee> validateSalaryConsistency() {
        return repository.findAll().stream()
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
        // Build statistics via stream for now (can be replaced by query)
        Map<String, List<Employee>> grouped = repository.findAll().stream()
                .collect(Collectors.groupingBy(e -> {
                    String company = e.getCompanyName();
                    return (company == null || company.isBlank()) ? "unknown" : company;
                }));

        Map<String, CompanyStatistics> result = new HashMap<>();
        grouped.forEach((company, list) -> {
            int count = list.size();
            double avg = list.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
            String highest = list.stream().max(Comparator.comparingDouble(Employee::getSalary)).map(Employee::getFullName).orElse("");
            result.put(company, new CompanyStatistics(company, count, avg, highest));
        });
        return result;
    }

    // raport w formacie "Imię Nazwisko (email): aktualne_wynagrodzenie < bazowe_wynagrodzenie"
    public List<String> salaryConsistencyReport() {
        return validateSalaryConsistency().stream()
                .map(employee -> String.format("%s (%s): %.2f < %.2f", employee.getFullName(), employee.getEmail(), employee.getSalary(), employee.getPosition().getSalary()))
                .collect(Collectors.toList());
    }

    @Transactional
    public int importEmployeesTransactional(@Valid @NotNull List<@Valid Employee> employees) {
        if (employees == null) return 0;
        if (employees.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Employee list contains null elements");
        }
        repository.deleteAll();
        repository.saveAll(employees);
        return employees.size();
    }

    public Page<Employee> findAll(Specification<Employee> spec, Pageable pageable) {
        Pageable effectivePageable = pageable != null ? pageable : PageRequest.of(0, 20);
        return repository.findAll(spec, effectivePageable);
    }
}
