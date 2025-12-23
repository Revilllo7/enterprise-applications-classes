package tests.statistics;

import main.statistics.FindHighestPaidEmployee;
import main.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FindHighestPaidEmployeeTest {
    @Test
    void shouldReturnHighestPaidEmployee() {
        List<Employee> employees = List.of(
                new Employee("A", "a@a.com", "X", Position.STAZYSTA, 3000),
                new Employee("B", "b@b.com", "X", Position.MANAGER, 12000)
        );

        var result = new FindHighestPaidEmployee().execute(employees);
        assertTrue(result.isPresent());
        assertEquals("b@b.com", result.get().getEmail());
    }
}
