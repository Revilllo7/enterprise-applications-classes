package com.techcorp.employee.service;

import com.techcorp.employee.model.Department;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentServiceTest {

    @Test
    void addFindUpdateRemoveLifecycle() {
        DepartmentService svc = new DepartmentService();

        Department d = new Department();
        d.setName("R&D");
        d.setLocation("Warsaw");
        d.setBudget(12345.67);
        d.setManagerEmail("boss@example.com");

        Department created = svc.addDepartment(d);
        assertNotNull(created.getId());

        List<Department> all = svc.getAllDepartments();
        assertEquals(1, all.size());

        var fetched = svc.findById(created.getId());
        assertTrue(fetched.isPresent());
        assertEquals("R&D", fetched.get().getName());

        Department update = new Department(null, "R&D2", "Krakow", 9999.0, "boss2@example.com");
        var updated = svc.updateDepartment(created.getId(), update);
        assertTrue(updated.isPresent());
        assertEquals(created.getId(), updated.get().getId());
        assertEquals("R&D2", updated.get().getName());

        boolean removed = svc.removeDepartment(created.getId());
        assertTrue(removed);
        assertTrue(svc.findById(created.getId()).isEmpty());
    }
}
