package tests.analytics;

import main.analytics.GroupByPosition;
import main.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class GroupByPositionTest {
    @Test
    void shouldGroupEmployeesByPosition() {
        List<Employee> employees = List.of(
                new Employee("A", "a@a.com", "X", Position.MANAGER, 12000),
                new Employee("B", "b@b.com", "X", Position.MANAGER, 12000),
                new Employee("C", "c@c.com", "X", Position.STAZYSTA, 3000)
        );

        Map<Position, List<Employee>> grouped = new GroupByPosition().execute(employees);

        assertEquals(2, grouped.get(Position.MANAGER).size());
        assertEquals(1, grouped.get(Position.STAZYSTA).size());
    }
}
