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

    @Test
    void addEmployee_rejects_null_and_duplicates_and_blank_email() {
        assertFalse(service.addEmployee(null));
        assertFalse(service.addEmployee(new Employee("X", null, "T", Position.STAZYSTA, 3000)));
        assertFalse(service.addEmployee(new Employee("Y", " ", "T", Position.STAZYSTA, 3000)));
        // duplicate by email (case-insensitive)
        assertFalse(service.addEmployee(new Employee("Alice Clone", "ALICE@example.com", "TechCorp", Position.MANAGER, 12001)));
    }

    @Test
    void findByEmail_returns_optional_and_is_case_insensitive() {
        assertTrue(service.findByEmail("ALICE@EXAMPLE.COM").isPresent());
        assertTrue(service.findByEmail("bob@example.com").isPresent());
        assertTrue(service.findByEmail(null).isEmpty());
        assertTrue(service.findByEmail(" ").isEmpty());
        assertTrue(service.findByEmail("unknown@example.com").isEmpty());
    }

    @Test
    void salaryConsistencyReport_formats_expected_lines() {
        List<String> report = service.salaryConsistencyReport();
        assertEquals(1, report.size());
        assertTrue(report.get(0).startsWith("Charlie Chan (charlie@example.com): "));
        assertTrue(report.get(0).contains("<"));
    }
}
