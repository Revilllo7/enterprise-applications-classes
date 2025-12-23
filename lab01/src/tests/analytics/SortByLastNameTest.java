package tests.analytics;

import main.analytics.SortByLastName;
import main.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SortByLastNameTest {
    @Test
    void shouldSortEmployeesAlphabeticallyByLastName() {
        List<Employee> employees = List.of(
                new Employee("Kasia Lis", "k@a.com", "X", Position.STAZYSTA, 3000),
                new Employee("Adam Nowak", "a@b.com", "X", Position.MANAGER, 12000)
        );

        var sorted = new SortByLastName().execute(employees);
        assertEquals("Lis", sorted.getFirst().getFullName().split(" ")[1]);
    }
}
