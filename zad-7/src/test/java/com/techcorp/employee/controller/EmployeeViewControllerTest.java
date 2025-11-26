package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EmployeeViewControllerTest {

    EmployeeService employeeService;
    EmployeeViewController controller;

    @BeforeEach
    void setUp() {
        employeeService = Mockito.mock(EmployeeService.class);
        controller = new EmployeeViewController(employeeService);
    }

    @Test
    void listEmployeesAddsModelAndReturnsView() {
        Employee e = new Employee("John Doe", "john@example.com", "ACME", Position.PROGRAMISTA, 8000);
        when(employeeService.getAllEmployees()).thenReturn(List.of(e));

        var model = new ConcurrentModel();
        String view = controller.listEmployees(model);
        assertEquals("employees/list", view);
        assertTrue(model.containsAttribute("employees"));
    }

    @Test
    void showAddFormProvidesDtoAndEnums() {
        var model = new ConcurrentModel();
        String view = controller.showAddForm(model);
        assertEquals("employees/add-form", view);
        assertTrue(model.containsAttribute("employee"));
        assertTrue(model.containsAttribute("positions"));
        assertTrue(model.containsAttribute("statuses"));
    }

    @Test
    void addEmployeeCallsServiceAndRedirects() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("new@x.com");
        when(employeeService.addEmployee(any())).thenReturn(true);

        var flash = new RedirectAttributesModelMap();
        String rv = controller.addEmployee(dto, flash);
        assertEquals("redirect:/employees", rv);
        assertTrue(flash.getFlashAttributes().containsKey("message"));
    }

    @Test
    void showEditFormWhenExists() {
        Employee e = new Employee("Jane Doe", "jane@x.com", "Co", Position.PROGRAMISTA, 8000);
        when(employeeService.findByEmail("jane@x.com")).thenReturn(Optional.of(e));

        var model = new ConcurrentModel();
        String rv = controller.showEditForm("jane@x.com", model, new RedirectAttributesModelMap());
        assertEquals("employees/edit-form", rv);
        assertTrue(model.containsAttribute("employee"));
        assertTrue(model.containsAttribute("positions"));
        assertTrue(model.containsAttribute("statuses"));
    }

    @Test
    void editEmployeeUpdatesAndRedirects() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("jane@x.com");
        when(employeeService.updateEmployee(any(), any())).thenReturn(Optional.of(new Employee("Jane Doe","jane@x.com","Co",Position.PROGRAMISTA,8000)));

        var flash = new RedirectAttributesModelMap();
        String rv = controller.editEmployee(dto, flash);
        assertEquals("redirect:/employees", rv);
        assertTrue(flash.getFlashAttributes().containsKey("message"));
    }

    @Test
    void deleteEmployeeRemovesAndRedirects() {
        when(employeeService.removeEmployee("x@x.com")).thenReturn(true);
        var flash = new RedirectAttributesModelMap();
        String rv = controller.deleteEmployee("x@x.com", flash);
        assertEquals("redirect:/employees", rv);
        assertTrue(flash.getFlashAttributes().containsKey("message"));
    }

    @Test
    void searchByCompanyReturnsResults() {
        Employee e = new Employee("X", "x@x.com", "Acme", Position.PROGRAMISTA, 8000);
        when(employeeService.getAllEmployees()).thenReturn(List.of(e));

        var model = new ConcurrentModel();
        String rv = controller.searchByCompany("Acme", model);
        assertEquals("employees/search-results", rv);
        assertTrue(model.containsAttribute("employees"));
        assertEquals("Acme", model.getAttribute("query"));
    }
}
