package com.techcorp.employee.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {
    @Test
    void addEmployee_sets_company_and_default_position_when_missing() {
        Company c = new Company("Acme");
        Employee e = new Employee("John", "john@ex.com", null, null, 0);

        assertTrue(c.addEmployee(e));
        assertEquals("Acme", e.getCompanyName());
        assertEquals(Position.STAZYSTA, e.getPosition());

        // cannot add again (duplicate equals by email)
        assertFalse(c.addEmployee(e));
    }

    @Test
    void removeEmployee_clears_company_and_position() {
        Company c = new Company("Acme");
        Employee e = new Employee("John", "john@ex.com", null, null, 0);
        assertTrue(c.addEmployee(e));

        assertTrue(c.removeEmployee(e));
        assertNull(e.getCompanyName());
        assertNull(e.getPosition());
        assertFalse(c.getEmployees().contains(e));
    }

    @Test
    void findEmployeeByEmail_and_collections() {
        Company c = new Company("Acme");
        Employee a = new Employee("Ann", "a@ex.com", null, Position.PROGRAMISTA, Position.PROGRAMISTA.getSalary());
        Employee b = new Employee("Bob", "b@ex.com", null, Position.MANAGER, Position.MANAGER.getSalary());
        c.addEmployee(a);
        c.addEmployee(b);

        assertEquals("Ann", c.findEmployeeByEmail("a@ex.com").getFullName());

        Set<Employee> alpha = c.getAlphabeticalEmployees();
        assertEquals(2, alpha.size());

        Map<Position, HashSet<Employee>> byPos = c.groupEmployeesByPosition();
        assertEquals(2, byPos.size());

        Map<Position, Integer> countByPos = c.countEmployeesByPosition();
        assertEquals(1, countByPos.get(Position.PROGRAMISTA));
        assertEquals(1, countByPos.get(Position.MANAGER));
    }

    @Test
    void salary_stats_and_highest_paid() {
        Company c = new Company("Acme");
        Employee a = new Employee("Ann", "a@ex.com", null, Position.PROGRAMISTA, 9000);
        Employee b = new Employee("Bob", "b@ex.com", null, Position.MANAGER, 12000);
        c.addEmployee(a);
        c.addEmployee(b);

        assertEquals((9000 + 12000) / 2.0, c.averageSalary(), 0.001);

        Optional<Employee> top = c.getHighestPaidEmployee();
        assertTrue(top.isPresent());
        assertEquals("Bob", top.get().getFullName());
    }
}
