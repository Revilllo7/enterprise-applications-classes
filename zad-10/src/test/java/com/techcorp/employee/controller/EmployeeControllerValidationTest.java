package com.techcorp.employee.controller;

import com.techcorp.employee.config.AppConfig;
import com.techcorp.employee.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(AppConfig.class)
class EmployeeControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("POST with invalid email domain returns 400 and field errors JSON")
    void postInvalidEmailDomain() throws Exception {
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
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.email").value(org.hamcrest.Matchers.containsString("@techcorp.com")));
    }

    @Test
    @DisplayName("POST with negative salary returns 400 and field error JSON")
    void postNegativeSalary() throws Exception {
        String payload = "{" +
                "\"firstName\":\"Jan\"," +
                "\"lastName\":\"Kowalski\"," +
                "\"email\":\"jan@techcorp.com\"," +
                "\"company\":\"TechCorp\"," +
                "\"position\":\"PROGRAMISTA\"," +
                "\"salary\":-5000.0," +
                "\"status\":\"ACTIVE\"" +
                "}";

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.salary").exists());
    }
}
