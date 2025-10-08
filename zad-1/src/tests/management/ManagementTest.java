package tests.management;

import main.management.AddEmployeeToSystem;
import main.management.validation.EmployeeValidation;
import main.model.Employee;
import main.model.Position;
import main.management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManagementTest {
    private EmployeeRepository repo;
    private AddEmployeeToSystem addService;

    @BeforeEach
    void setup() {
        repo = new EmployeeRepository();
        addService = new AddEmployeeToSystem(repo, new EmployeeValidation());
    }

    @Test
    void shouldAddEmployeeWhenEmailIsUnique() {
        Employee emp = new Employee("Adam Smith", "adam@techcorp.com", "TechCorp", Position.PROGRAMISTA, 8000);
        boolean result = addService.add(emp);
        assertTrue(result);
        assertEquals(1, repo.getEmployees().size());
    }

    @Test
    void shouldRejectEmployeeWhenEmailAlreadyExists() {
        Employee emp1 = new Employee("Adam Smith", "adam@techcorp.com", "TechCorp", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Adam Johnson", "adam@techcorp.com", "TechCorp", Position.MANAGER, 12000);
        addService.add(emp1);
        boolean result = addService.add(emp2);
        assertFalse(result);
        assertEquals(1, repo.getEmployees().size());
    }
}
