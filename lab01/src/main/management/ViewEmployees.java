package main.management;

import main.model.Employee;
import main.management.repository.EmployeeRepository;
import java.util.List;

public class ViewEmployees {
    private final EmployeeRepository repository;

    public ViewEmployees(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> viewAll() {
        return repository.getEmployees();
    }

    public void printAll() {
        repository.getEmployees().forEach(System.out::println);
    }
}