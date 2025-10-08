package main.management.validation;

import main.model.Employee;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class EmployeeValidation {
    public boolean isEmailUnique(String email, @NotNull Collection<Employee> employees) {
        return employees.stream().noneMatch(employee -> employee.getEmail().equalsIgnoreCase(email));
    }
}