package service;

import model.Employee;
import model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceTest {
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService();
        service.addEmployee(new Employee("Alice Doe", "alice@example.com", "TechCorp", Position.MANAGER, 12001));
        service.addEmployee(new Employee("Bob Smith", "bob@example.com", "TechCorp", Position.PROGRAMISTA, 9000));
        service.addEmployee(new Employee("Charlie Chan", "charlie@example.com", "CodeWorks", Position.PROGRAMISTA, 7500));
    }

    @Test
    void validateSalaryConsistency_shouldFindEmployeesBelowBase() {
        List<Employee> underpaid = service.validateSalaryConsistency();
        assertEquals(1, underpaid.size());
        assertEquals("Charlie Chan", underpaid.get(0).getFullName());
    }

    @Test
    void getCompanyStatistics_shouldReturnAggregatedData() {
        Map<String, ?> stats = service.getCompanyStatistics();
        assertEquals(2, stats.size());
        assertTrue(stats.containsKey("TechCorp"));
    }
}
