package com.techcorp.employee.service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techcorp.employee.dao.EmployeeDAO;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;

/**
 * Serwis przechowujący pracowników i udostępniający operacje analityczne.
 * Wszystkie operacje na kolekcjach wykonane przez Stream API.
 */
@Service
public class EmployeeService {
    private final EmployeeDAO dao;
    public EmployeeService(EmployeeDAO dao) {
        this.dao = dao;
    }

    // No-arg constructor kept for tests and simple usage without Spring context.
    // Uses an in-memory DAO that mimics previous behavior.
    public EmployeeService() {
        this.dao = new InMemoryEmployeeDAO();
    }

    // Simple in-memory DAO used by tests when no DAO is provided.
    // Cause I can't be bothered re-writing all the tests
    static class InMemoryEmployeeDAO implements EmployeeDAO {
        private final Map<String, Employee> map = new HashMap<>();

        @Override
        public List<Employee> findAll() {
            return new ArrayList<>(map.values());
        }

        @Override
        public Optional<Employee> findByEmail(String email) {
            if (email == null) return Optional.empty();
            return Optional.ofNullable(map.get(email.toLowerCase(Locale.ROOT)));
        }

        @Override
        public void save(Employee employee) {
            if (employee == null || employee.getEmail() == null) return;
            map.put(employee.getEmail().toLowerCase(Locale.ROOT), employee);
        }

        @Override
        public void delete(String email) {
            if (email == null) return;
            map.remove(email.toLowerCase(Locale.ROOT));
        }

        @Override
        public void deleteAll() {
            map.clear();
        }

        @Override
        public java.util.List<com.techcorp.employee.model.CompanyStatistics> getCompanyStatistics() {
            Map<String, List<Employee>> grouped = map.values().stream()
                    .collect(Collectors.groupingBy(employee -> {
                        String company = employee.getCompanyName();
                        return (company == null || company.trim().isBlank()) ? "unknown" : company.trim();
                    }));

            List<com.techcorp.employee.model.CompanyStatistics> result = new ArrayList<>();
            for (Map.Entry<String, List<Employee>> entry : grouped.entrySet()) {
                String company = entry.getKey();
                List<Employee> list = entry.getValue();
                int count = list.size();
                double avg = list.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
                String highest = list.stream().max(Comparator.comparingDouble(Employee::getSalary)).map(Employee::getFullName).orElse("");
                result.add(new com.techcorp.employee.model.CompanyStatistics(company, count, avg, highest));
            }
            return result;
        }
    }

    public boolean addEmployee(Employee employee) {
        if (employee == null || employee.getEmail() == null || employee.getEmail().isBlank()) return false;
        String emailKey = employee.getEmail().toLowerCase(Locale.ROOT);
        if (dao.findByEmail(emailKey).isPresent()) return false;
        dao.save(employee);
        return true;
    }

    public List<Employee> getAllEmployees() {
        return dao.findAll();
    }

    public Optional<Employee> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return dao.findByEmail(email.toLowerCase(Locale.ROOT));
    }

    public List<Employee> findByStatus(com.techcorp.employee.model.EmploymentStatus status) {
        if (status == null) return List.of();
        return dao.findAll().stream()
                .filter(e -> status.equals(e.getStatus()))
                .collect(Collectors.toList());
    }

    public boolean updateStatus(String email, com.techcorp.employee.model.EmploymentStatus status) {
        if (email == null || email.isBlank() || status == null) return false;
        String key = email.toLowerCase(Locale.ROOT);
        Optional<Employee> existingOpt = dao.findByEmail(key);
        if (existingOpt.isEmpty()) return false;
        Employee existing = existingOpt.get();
        existing.setStatus(status);
        dao.save(existing);
        return true;
    }

    public java.util.Map<com.techcorp.employee.model.EmploymentStatus, Long> getStatusDistribution() {
        return dao.findAll().stream()
                .collect(Collectors.groupingBy(Employee::getStatus, Collectors.counting()));
    }

    public boolean removeEmployee(String email) {
        if (email == null || email.isBlank()) return false;
        String key = email.toLowerCase(Locale.ROOT);
        Optional<Employee> existing = dao.findByEmail(key);
        if (existing.isEmpty()) return false;
        dao.delete(key);
        return true;
    }

    public Optional<Employee> updateEmployee(String email, Employee updated) {
        if (email == null || email.isBlank() || updated == null) return Optional.empty();
        String key = email.toLowerCase(Locale.ROOT);
        Optional<Employee> existingOpt = dao.findByEmail(key);
        if (existingOpt.isEmpty()) return Optional.empty();
        Employee existing = existingOpt.get();

        // create a new Employee instance preserving the identity (email)
        String newFullName = updated.getFullName() == null ? existing.getFullName() : updated.getFullName();
        String newCompany = updated.getCompanyName();
        Position newPosition = updated.getPosition();
        double newSalary = updated.getSalary();

        Employee replacement = new Employee(existing.getId(), newFullName, existing.getEmail(), newCompany, newPosition, newSalary);
        dao.save(replacement);
        return Optional.of(replacement);
    }



     // Zwraca listę pracowników, których wynagrodzenie jest niższe niż bazowe dla ich stanowiska.
    public List<Employee> validateSalaryConsistency() {
        return dao.findAll().stream()
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
        List<CompanyStatistics> stats = dao.getCompanyStatistics();
        Map<String, CompanyStatistics> map = new HashMap<>();
        for (CompanyStatistics cs : stats) {
            String name = cs.getCompanyName();
            if (name == null || name.isBlank()) name = "unknown";
            map.put(name, cs);
        }
        return map;
    }

    // raport w formacie "Imię Nazwisko (email): aktualne_wynagrodzenie < bazowe_wynagrodzenie"
    public List<String> salaryConsistencyReport() {
        return validateSalaryConsistency().stream()
                .map(employee -> String.format("%s (%s): %.2f < %.2f", employee.getFullName(), employee.getEmail(), employee.getSalary(), employee.getPosition().getSalary()))
                .collect(Collectors.toList());
    }

    @Transactional
    public int importEmployeesTransactional(List<Employee> employees) {
        if (employees == null) return 0;
        dao.deleteAll();
        for (Employee e : employees) {
            dao.save(e);
        }
        return employees.size();
    }
}
