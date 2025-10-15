package service;

import model.Employee;
import model.Position;
import model.CompanyStatistics;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;

/**
 * Serwis przechowujący pracowników i udostępniający operacje analityczne.
 * Wszystkie operacje na kolekcjach wykonane przez Stream API.
 */
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


    /**
     * Zwraca listę pracowników, których wynagrodzenie jest niższe niż baza stanowiska.
     */
    public List<Employee> validateSalaryConsistency() {
        return employeesByEmail.values().stream()
                .filter(employee -> {
                    Position position = employee.getPosition();
                    if (position == null) return false;
                    return employee.getSalary() < position.getBaseSalary();
                })
                .sorted(Comparator.comparing(Employee::getFullName))
                .collect(Collectors.toList());
    }

    /**
     * Dla wszystkich firm tworzy mapę: companyName -> CompanyStatistics
     */
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

    /**
     * Raport o niespójności wynagrodzenia w stosunku do bazy stanowiska.
     */
    public List<String> salaryConsistencyReport() {
        return validateSalaryConsistency().stream()
                .map(employee -> String.format("%s (%s): %.2f < %.2f", employee.getFullName(), employee.getEmail(), employee.getSalary(), employee.getPosition().getBaseSalary()))
                .collect(Collectors.toList());
    }
}
