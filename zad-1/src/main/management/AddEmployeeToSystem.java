package main.management;

import main.model.Employee;
import main.management.repository.EmployeeRepository;
import main.management.validation.EmployeeValidation;
import org.jetbrains.annotations.NotNull;

public class AddEmployeeToSystem {
    private final EmployeeRepository repository;
    private final EmployeeValidation validator;

    public AddEmployeeToSystem(EmployeeRepository repository, EmployeeValidation validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public boolean add(@NotNull Employee employee) {
        if (!validator.isEmailUnique(employee.getEmail(), repository.getEmployees())) {
            System.out.println("email already exists. Not an unique identifier: " + employee.getEmail());
            System.out.println("Did not add this employee into the system: " + employee.getFullName());
            return false;
        }
        repository.add(employee);
        System.out.println("employee added into the system: " + employee.getFullName());
        return true;
    }
}
