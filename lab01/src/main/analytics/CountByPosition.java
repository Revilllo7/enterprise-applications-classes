package main.analytics;

import main.model.Employee;
import main.model.Position;
import java.util.*;
import java.util.stream.Collectors;

public class CountByPosition {
    public Map<Position, Long> execute(@org.jetbrains.annotations.NotNull List<Employee> employees) {
        return employees.stream().collect(Collectors.groupingBy(Employee::getPosition, Collectors.counting()));
    }
}