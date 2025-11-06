package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {
    @Test
    void equals_and_hashCode_are_case_insensitive_by_email() {
        Employee a = new Employee("Ann Example", "USER@MAIL.com", "Acme", Position.PROGRAMISTA, 8000);
        Employee b = new Employee("Another Name", "user@mail.COM", "Other", Position.MANAGER, 12000);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void setPosition_updates_salary_to_base_for_position() {
        Employee e = new Employee("John Doe", "john@example.com", "Acme", Position.STAZYSTA, 3000);
        assertEquals(3000, e.getSalary(), 0.001);

        e.setPosition(Position.PROGRAMISTA);
        assertEquals(Position.PROGRAMISTA, e.getPosition());
        assertEquals(Position.PROGRAMISTA.getSalary(), e.getSalary(), 0.001);
    }

    @Test
    void toString_contains_key_fields() {
        Employee e = new Employee("Jane Roe", "jane@example.com", "Acme", Position.MANAGER, 12000);
        String s = e.toString();
        assertTrue(s.contains("Jane Roe"));
        assertTrue(s.contains("jane@example.com"));
        assertTrue(s.contains("Acme"));
        assertTrue(s.contains("MANAGER"));
    }
}
