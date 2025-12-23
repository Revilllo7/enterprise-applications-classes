package main.analytics;

import main.model.Employee;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class FindEmployeeByCompanyName {
    public List<Employee> execute(@NotNull List<Employee> employees, String companyName) {
        return employees.stream().filter(employee -> employee.getCompanyName().equalsIgnoreCase(companyName)).collect(Collectors.toList());
    }
}