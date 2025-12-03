package com.techcorp.employee.service;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeServiceDAOTest {

    @Test
    void addFindAndRemoveEmployee() {
        EmployeeService service = new EmployeeService();
        Employee e = new Employee(null, "Jan Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8000);

        assertTrue(service.addEmployee(e));
        Optional<Employee> found = service.findByEmail("jan@example.com");
        assertTrue(found.isPresent());
        assertEquals("Jan Kowalski", found.get().getFullName());

        assertTrue(service.removeEmployee("jan@example.com"));
        assertFalse(service.findByEmail("jan@example.com").isPresent());
    }

    @Test
    void duplicateNotAllowed() {
        EmployeeService service = new EmployeeService();
        Employee e1 = new Employee(null, "A B", "a@example.com", "C", Position.MANAGER, 12000);
        Employee e2 = new Employee(null, "A B2", "a@example.com", "C", Position.MANAGER, 12000);

        assertTrue(service.addEmployee(e1));
        assertFalse(service.addEmployee(e2));
    }

    @Test
    void updateEmployeeAndStatus() {
        EmployeeService service = new EmployeeService();
        Employee e = new Employee(null, "Z Imie", "z@example.com", "X", Position.STAZYSTA, 3000);
        service.addEmployee(e);

        Optional<Employee> before = service.findByEmail("z@example.com");
        assertTrue(before.isPresent());

        assertTrue(service.updateStatus("z@example.com", EmploymentStatus.ON_LEAVE));
        Optional<Employee> after = service.findByEmail("z@example.com");
        assertTrue(after.isPresent());
        assertEquals(EmploymentStatus.ON_LEAVE, after.get().getStatus());

        Employee updated = new Employee(after.get().getId(), "Z New", "z@example.com", "Y", Position.PROGRAMISTA, 8000);
        Optional<Employee> res = service.updateEmployee("z@example.com", updated);
        assertTrue(res.isPresent());
        assertEquals("Z New", res.get().getFullName());
        assertEquals(8000, res.get().getSalary());
    }

    @Test
    void importTransactionalReplacesAll() {
        EmployeeService service = new EmployeeService();
        Employee a = new Employee(null, "A One", "a1@example.com", "C1", Position.PROGRAMISTA, 8000);
        Employee b = new Employee(null, "B Two", "b2@example.com", "C2", Position.MANAGER, 12000);
        service.addEmployee(a);
        service.addEmployee(b);

        List<Employee> all = service.getAllEmployees();
        assertEquals(2, all.size());

        Employee n1 = new Employee(null, "New One", "n1@example.com", "NC", Position.PREZES, 25000);
        service.importEmployeesTransactional(List.of(n1));

        List<Employee> after = service.getAllEmployees();
        assertEquals(1, after.size());
        assertEquals("n1@example.com", after.get(0).getEmail());
    }
}
