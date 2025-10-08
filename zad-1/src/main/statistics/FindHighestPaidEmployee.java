package main.statistics;

import main.model.Employee;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FindHighestPaidEmployee {
    public Optional<Employee> execute(@NotNull List<Employee> employees) {
        return employees.stream().max(Comparator.comparing(Employee::getSalary));
    }
}