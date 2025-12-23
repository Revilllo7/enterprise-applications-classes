package main.analytics.util;

import main.model.Employee;
import java.util.Comparator;

public class EmployeeComparators {
    public static final Comparator<Employee> BY_LAST_NAME =
            Comparator.comparing(employee -> employee.getFullName().split(" ")[1]);
}