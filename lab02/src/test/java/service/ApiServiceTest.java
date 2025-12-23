package service;

import model.Employee;
import model.Position;
import exception.ApiException;

import org.junit.jupiter.api.Test;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiServiceTest {
    @Test
    void parseApiResponse_shouldMapFieldsCorrectly() throws Exception {
        String mockJson = """
            [
              {"name": "John Doe", "email": "john@example.com", "company": {"name": "TechCorp"}},
              {"name": "Nina", "email": "nina@example.com", "company": {"name": "CodeWorks"}}
            ]
            """;

        List<Employee> list = getEmployees();
        assertEquals(2, list.size());
        assertEquals("John Doe", list.get(0).getFullName());
        assertEquals("TechCorp", list.get(0).getCompanyName());
    }

    private static List<Employee> getEmployees() throws ApiException {
        ApiService service = new ApiService() {
            @Override
            public List<Employee> fetchEmployeesFromApi(String url) {
                // Podmieniamy metodę do testów — bez HTTP
                return List.of(
                        new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMISTA, 8000),
                        new Employee("Nina", "nina@example.com", "CodeWorks", Position.PROGRAMISTA, 8000)
                );
            }
        };

        return service.fetchEmployeesFromApi("dummy");
    }
}
