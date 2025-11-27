package com.techcorp.employee.controller;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.DepartmentService;
import com.techcorp.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepartmentViewControllerTest {

    DepartmentService deptSvc;
    EmployeeService empSvc;
    com.techcorp.employee.service.FileStorageService fileStorageService;
    DepartmentViewController controller;

    @BeforeEach
    void setUp() {
        deptSvc = Mockito.mock(DepartmentService.class);
        empSvc = Mockito.mock(EmployeeService.class);
        fileStorageService = Mockito.mock(com.techcorp.employee.service.FileStorageService.class);
        controller = new DepartmentViewController(deptSvc, empSvc, fileStorageService);
    }

    @Test
    void listShouldReturnDepartmentsAndManagerNames() {
        Department d = new Department(1L, "R&D", "WAW", 1000.0, "mgr@example.com");
        when(deptSvc.getAllDepartments()).thenReturn(List.of(d));

        Employee e = new Employee("Mgr One", "mgr@example.com", "Tech", Position.MANAGER, 12000);
        when(empSvc.getAllEmployees()).thenReturn(List.of(e));
        when(empSvc.findByEmail("mgr@example.com")).thenReturn(Optional.of(e));

        var model = new ConcurrentModel();
        String view = controller.list(model);
        assertEquals("departments/list", view);
        assertTrue(model.containsAttribute("departments"));
        assertTrue(model.containsAttribute("managerNames"));
    }

    @Test
    void showAddFormProvidesManagers() {
        Employee m1 = new Employee("Boss", "b@x.com", "Co", Position.MANAGER, 12000);
        Employee dev = new Employee("Dev", "d@x.com", "Co", Position.PROGRAMISTA, 8000);
        when(empSvc.getAllEmployees()).thenReturn(List.of(m1, dev));

        var model = new ConcurrentModel();
        String view = controller.showAddForm(model);
        assertEquals("departments/form", view);
        assertTrue(model.containsAttribute("department"));
        assertTrue(model.containsAttribute("managers"));
        var managers = (List<?>) model.getAttribute("managers");
        assertEquals(1, managers.size());
    }

    @Test
    void saveDepartmentCreatesAndRedirects() {
        var dept = new Department(null, "Ops", "PL", 500.0, null);
        when(deptSvc.addDepartment(any())).thenReturn(dept);

        var flash = new RedirectAttributesModelMap();
        var model = new ConcurrentModel();
        var binding = new org.springframework.validation.BeanPropertyBindingResult(dept, "department");
        String rv = controller.saveDepartment(dept, binding, model, flash);
        assertEquals("redirect:/departments", rv);
        assertTrue(flash.getFlashAttributes().containsKey("message"));
    }

    @Test
    void showEditFormNotFoundRedirects() {
        when(deptSvc.findById(99L)).thenReturn(Optional.empty());
        var flash = new RedirectAttributesModelMap();
        String rv = controller.showEditForm(99L, new ConcurrentModel(), flash);
        assertEquals("redirect:/departments", rv);
        assertTrue(flash.getFlashAttributes().containsKey("error"));
    }

    @Test
    void detailsShowsEmployeesAndManager() {
        Department d = new Department(2L, "Sales", "PL", 200.0, "mgr@x.com");
        when(deptSvc.findById(2L)).thenReturn(Optional.of(d));

        Employee e1 = new Employee("Alice", "a@x.com", "Co", Position.PROGRAMISTA, 8000);
        e1.setDepartmentId(2L);
        when(empSvc.getAllEmployees()).thenReturn(List.of(e1));
        when(empSvc.findByEmail("mgr@x.com")).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        String rv = controller.details(2L, model, new RedirectAttributesModelMap());
        assertEquals("departments/details", rv);
        assertTrue(model.containsAttribute("employees"));
        assertTrue(model.containsAttribute("managerName"));
    }
}
