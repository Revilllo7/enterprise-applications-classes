package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PositionTest {
    @Test
    void valueOf_and_baseSalary_are_consistent() {
        // ensure enum value exists and base salary getter works
        Position p = Position.valueOf("PROGRAMISTA");
        assertNotNull(p);
        assertTrue(p.getBaseSalary() > 0, "Base salary should be positive for PROGRAMISTA");
    }

    @Test
    void all_positions_have_non_negative_baseSalary() {
        for (Position pos : Position.values()) {
            assertTrue(pos.getBaseSalary() >= 0, "Position base salary must be >= 0 for " + pos);
        }
    }
}