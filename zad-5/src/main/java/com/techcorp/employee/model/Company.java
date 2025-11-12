package com.techcorp.employee.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Company {
    private final HashSet<Employee> employees;
    private final String name;

    public Company(String name) {
        this.name = name;
        this.employees = new HashSet<>();
    }

    public boolean addEmployee(Employee employee) {
        if(employee.getCompanyName() == null && employees.add(employee)) {
            employee.companyName = name;
            if(employee.getPosition() == null) {
                employee.setPosition(Position.STAZYSTA);
            }
            return true;
        }
        return false;
    }

    public boolean removeEmployee(Employee employee) {
        if (Objects.equals(employee.companyName, this.name) && employees.remove(employee)) {
            employee.companyName = null;
            employee.setPosition(null);
            return true;
        }
        return false;
    }

    public HashSet<Employee> getEmployees() {
        return employees;
    }

    public Employee findEmployeeByEmail(String email) {
        return employees.stream().filter(e -> e.getEmail().equals(email)).findFirst().orElse(null);
    }

    public Set<Employee> getAlphabeticalEmployees() {
        return employees.stream().sorted(Comparator.comparing(Employee::getFullName)).collect(Collectors.toSet());
    }

    public Map<Position, HashSet<Employee>> groupEmployeesByPosition() {
        return employees.stream().collect(Collectors.groupingBy(Employee::getPosition, Collectors.toCollection(HashSet::new)));
    }

    public Map<Position, Integer> countEmployeesByPosition() {
        return employees.stream().collect(Collectors.groupingBy(Employee::getPosition, Collectors.reducing(0, employee -> 1, Integer::sum)));
    }

    public Double averageSalary() {
        return employees.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
    }

    public Optional<Employee> getHighestPaidEmployee() {
        return employees.stream().max(Comparator.comparing(Employee::getSalary));
    }
}
