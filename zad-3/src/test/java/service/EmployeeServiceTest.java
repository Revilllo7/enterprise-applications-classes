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

    @Test
    void getCompanyStatistics_emptyService_returnsEmptyMap() {
        EmployeeService emptyService = new EmployeeService();
        assertTrue(emptyService.getCompanyStatistics().isEmpty());
    }

    @Test
    void getCompanyStatistics_nullCompanyMappedToUnknown() {
        EmployeeService s = new EmployeeService();
        s.addEmployee(new Employee("No Company", "no@c.com", null, Position.STAZYSTA, 3000));
        Map<String, ?> stats = s.getCompanyStatistics();
        assertTrue(stats.containsKey("unknown"));
    }

    @Test
    void validateSalaryConsistency_ignores_nullPosition_and_notFlag_equalBase() {
        EmployeeService s = new EmployeeService();
        // employee with null position should be ignored
        s.addEmployee(new Employee("Null Pos", "nullpos@example.com", "X", null, 1000));
        // employee with salary == base should NOT be underpaid
        double base = Position.PROGRAMISTA.getSalary();
        s.addEmployee(new Employee("Exact Base", "exact@example.com", "X", Position.PROGRAMISTA, (int) base));
        List<Employee> underpaid = s.validateSalaryConsistency();
        assertTrue(underpaid.stream().noneMatch(e -> e.getEmail().equals("nullpos@example.com")));
        assertTrue(underpaid.stream().noneMatch(e -> e.getEmail().equals("exact@example.com")));
    }

    @Test
    void validateSalaryConsistency_base_doesnt_equal_underpaid() {
        EmployeeService s = new EmployeeService();
        double base = Position.MANAGER.getSalary();
        s.addEmployee(new Employee("Below Base", "below@example.com", "X", Position.MANAGER, (int) base));
        List<Employee> notUnderpaid = s.validateSalaryConsistency();
        assertFalse(notUnderpaid.stream().anyMatch(e -> e.getEmail().equals("below@example.com")));
    }
}
