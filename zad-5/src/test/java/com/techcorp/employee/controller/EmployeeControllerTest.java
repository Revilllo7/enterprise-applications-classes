package com.techcorp.employee.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.techcorp.employee.controller.EmployeeController;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;

import org.springframework.boot.test.context.TestConfiguration;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @TestConfiguration
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private Employee sampleEmployee() {
        return new Employee("Jan Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8000.0);
    }

    @Test
    @DisplayName("GET all employees returns 200 and JSON array")
    void getAllEmployees() throws Exception {
        Employee e = sampleEmployee();
        when(employeeService.getAllEmployees()).thenReturn(List.of(e));

        mockMvc.perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].email").value("jan@example.com"))
                .andExpect(jsonPath("$[0].firstName").value("Jan"));
    }

    @Test
    @DisplayName("GET employee by email returns 200 and employee data")
    void getByEmailFound() throws Exception {
        Employee e = sampleEmployee();
        when(employeeService.findByEmail("jan@example.com")).thenReturn(Optional.of(e));

        mockMvc.perform(get("/api/employees/jan@example.com").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jan@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jan"));
    }

    @Test
    @DisplayName("GET non-existing employee returns 404")
    void getByEmailNotFound() throws Exception {
        when(employeeService.findByEmail("noone@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/noone@example.com").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST new employee returns 201 and Location header")
    void postCreateEmployee() throws Exception {
        Employee e = sampleEmployee();
        // service will accept creation
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(true);
        when(employeeService.findByEmail("jan@example.com")).thenReturn(Optional.of(e));

        String payload = "{" +
                "\"firstName\":\"Jan\"," +
                "\"lastName\":\"Kowalski\"," +
                "\"email\":\"jan@example.com\"," +
                "\"company\":\"TechCorp\"," +
                "\"position\":\"PROGRAMISTA\"," +
                "\"salary\":8000.0," +
                "\"status\":\"ACTIVE\"" +
                "}";

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/employees/jan@example.com")))
                .andExpect(jsonPath("$.email").value("jan@example.com"));
    }

    @Test
    @DisplayName("POST duplicate returns 409 Conflict")
    void postDuplicate() throws Exception {
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(false);
        String payload = "{" +
                "\"firstName\":\"Jan\"," +
                "\"lastName\":\"Kowalski\"," +
                "\"email\":\"jan@example.com\"," +
                "\"company\":\"TechCorp\"," +
                "\"position\":\"PROGRAMISTA\"," +
                "\"salary\":8000.0," +
                "\"status\":\"ACTIVE\"" +
                "}";

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE employee returns 204 No Content")
    void deleteEmployee() throws Exception {
        when(employeeService.removeEmployee("jan@example.com")).thenReturn(true);

        mockMvc.perform(delete("/api/employees/jan@example.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Filter employees by company")
    void filterByCompany() throws Exception {
        Employee e1 = new Employee("Jan Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8000.0);
        Employee e2 = new Employee("Anna Nowak", "anna@example.com", "OtherCo", Position.MANAGER, 12000.0);
        when(employeeService.getAllEmployees()).thenReturn(List.of(e1, e2));

        mockMvc.perform(get("/api/employees").param("company", "TechCorp").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("jan@example.com"));
    }

    @Test
    @DisplayName("PATCH update status returns 200 and updated employee")
    void patchStatus() throws Exception {
        Employee e = sampleEmployee();
        Employee updated = new Employee("Jan Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8000.0);
        updated.setStatus(EmploymentStatus.ON_LEAVE);

        when(employeeService.updateStatus("jan@example.com", EmploymentStatus.ON_LEAVE)).thenReturn(true);
        when(employeeService.findByEmail("jan@example.com")).thenReturn(Optional.of(updated));

        String payload = "{\"status\":\"ON_LEAVE\"}";

        mockMvc.perform(patch("/api/employees/jan@example.com/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON_LEAVE"));
    }
}
