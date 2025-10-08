package tests.analytics;

import main.analytics.CountByPosition;
import main.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class CountByPositionTest {
    @Test
    void shouldCountEmployeesPerPosition() {
        List<Employee> employees = List.of(
                new Employee("A", "a@a.com", "X", Position.PROGRAMISTA, 8000),
                new Employee("B", "b@b.com", "X", Position.PROGRAMISTA, 9000),
                new Employee("C", "c@c.com", "X", Position.MANAGER, 12000)
        );

        Map<Position, Long> counts = new CountByPosition().execute(employees);
        assertEquals(2L, counts.get(Position.PROGRAMISTA));
        assertEquals(1L, counts.get(Position.MANAGER));
    }
}
