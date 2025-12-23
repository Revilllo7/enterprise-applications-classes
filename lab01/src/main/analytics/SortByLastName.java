package main.analytics;

import main.analytics.util.EmployeeComparators;
import main.model.Employee;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SortByLastName {
    public List<Employee> execute(@NotNull List<Employee> employees) {
        return employees.stream().sorted(EmployeeComparators.BY_LAST_NAME).collect(Collectors.toList());
    }
}