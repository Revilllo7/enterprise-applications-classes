package tests.analytics;

import main.analytics.FindEmployeeByCompanyName;
import main.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FindEmployeeByCompanyNameTest {
    @Test
    void shouldReturnOnlyEmployeesFromSpecificCompany() {
        List<Employee> employees = List.of(
                new Employee("Anna Kowalska", "anna@techcorp.com", "TechCorp", Position.PROGRAMISTA, 8500),
                new Employee("Jan Nowak", "jan@devhouse.com", "DevHouse", Position.MANAGER, 12000)
        );

        var result = new FindEmployeeByCompanyName().execute(employees, "TechCorp");
        assertEquals(1, result.size());
        assertEquals("anna@techcorp.com", result.getFirst().getEmail());
    }
}
