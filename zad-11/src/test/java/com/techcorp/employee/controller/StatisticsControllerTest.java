package com.techcorp.employee.controller;

import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatisticsController.class)
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    // MockBean deprecated
    // MockitoBean cannot be resolved to a type
    // I AM KILLING MYSELF TONIGHT
    private EmployeeService employeeService;

    @Test
    void averageSalary_noCompany() throws Exception {
        Employee a = new Employee("A One", "a@x.com", "X", Position.PROGRAMISTA, 1000.0);
        Employee b = new Employee("B Two", "b@x.com", "Y", Position.PROGRAMISTA, 3000.0);
        when(employeeService.getAllEmployees()).thenReturn(List.of(a, b));

        mvc.perform(get("/api/statistics/salary/average").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(2000.0));
    }

    @Test
    void averageSalary_byCompany() throws Exception {
        Employee a = new Employee("A", "a@t.com", "TechCorp", Position.PROGRAMISTA, 8000.0);
        Employee b = new Employee("B", "b@t.com", "OtherCo", Position.PROGRAMISTA, 4000.0);
        when(employeeService.getAllEmployees()).thenReturn(List.of(a, b));

        mvc.perform(get("/api/statistics/salary/average").param("company", "TechCorp").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(8000.0));
    }

    @Test
    void companyStatistics_found() throws Exception {
        CompanyStatistics cs = new CompanyStatistics(2, 9000.0, "Top Person");
        when(employeeService.getCompanyStatistics()).thenReturn(Map.of("TechCorp", cs));
        Employee e1 = new Employee("Top Person", "top@t.com", "TechCorp", Position.PREZES, 12000.0);
        Employee e2 = new Employee("Worker", "w@t.com", "TechCorp", Position.PROGRAMISTA, 6000.0);
        when(employeeService.getAllEmployees()).thenReturn(List.of(e1, e2));

        mvc.perform(get("/api/statistics/company/TechCorp").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("TechCorp"))
                .andExpect(jsonPath("$.employeeCount").value(2))
                .andExpect(jsonPath("$.averageSalary").value(9000.0))
                .andExpect(jsonPath("$.highestSalary").value(12000.0))
                .andExpect(jsonPath("$.topEarnerName").value("Top Person"));
    }

    @Test
    void positions_counts() throws Exception {
        Employee a = new Employee("A", "a@x.com", "X", Position.PROGRAMISTA, 8000.0);
        Employee b = new Employee("B", "b@x.com", "X", Position.PROGRAMISTA, 8000.0);
        Employee c = new Employee("C", "c@x.com", "X", Position.MANAGER, 12000.0);
        when(employeeService.getAllEmployees()).thenReturn(List.of(a, b, c));

        mvc.perform(get("/api/statistics/positions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PROGRAMISTA").value(2))
                .andExpect(jsonPath("$.MANAGER").value(1));
    }

    @Test
    void status_distribution() throws Exception {
        when(employeeService.getStatusDistribution()).thenReturn(Map.of(
                EmploymentStatus.ACTIVE, 5L,
                EmploymentStatus.ON_LEAVE, 2L
        ));

        mvc.perform(get("/api/statistics/status").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ACTIVE").value(5))
                .andExpect(jsonPath("$.ON_LEAVE").value(2));
    }
}

