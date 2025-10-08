package main.statistics;

import main.model.Employee;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CountAverageSalary {
    public double execute(@NotNull List<Employee> employees) {
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
    }
}
