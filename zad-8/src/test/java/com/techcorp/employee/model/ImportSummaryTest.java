package com.techcorp.employee.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportSummaryTest {
    @Test
    void getFailedCount_handles_null_and_counts_errors() {
        ImportSummary a = new ImportSummary(2, null);
        assertEquals(0, a.getFailedCount());

        ImportSummary b = new ImportSummary(1, List.of("e1", "e2", "e3"));
        assertEquals(3, b.getFailedCount());
        assertTrue(b.toString().contains("importedCount=1"));
    }
}
