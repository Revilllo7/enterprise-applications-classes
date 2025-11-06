package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PositionTest {
    @Test
    void valueOf_and_baseSalary_are_consistent() {
        // sprawdź, czy można znaleźć po nazwie i czy baza jest sensowna
        Position position = Position.valueOf("PROGRAMISTA");
        assertNotNull(position);
        assertTrue(position.getSalary() > 0, "Base salary should be positive for PROGRAMISTA");
    }

    @Test
    void all_positions_have_non_negative_baseSalary() {
        for (Position position : Position.values()) {
            assertTrue(position.getSalary() >= 0, "Position base salary must be >= 0 for " + position);
        }
    }
}