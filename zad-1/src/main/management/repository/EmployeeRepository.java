package main.management.repository;

import main.model.Employee;
import java.util.*;

public class EmployeeRepository {
    private final Map<String, Employee> employees = new HashMap<>();

    public void add(Employee employee) {
        employees.put(employee.getEmail(), employee);
    }

    public List<Employee> getEmployees() {
        return new ArrayList<>(employees.values());
    }

    public Optional<Employee> findByEmail(String email) {
        return Optional.ofNullable(employees.get(email));
    }
}
