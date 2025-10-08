package main.analytics;

import main.model.Employee;
import main.model.Position;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class GroupByPosition {
    public Map<Position, List<Employee>> execute(@NotNull List<Employee> employees) {
        return employees.stream().collect(Collectors.groupingBy(Employee::getPosition));
    }
}