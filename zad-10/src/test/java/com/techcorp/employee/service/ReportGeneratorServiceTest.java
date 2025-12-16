package com.techcorp.employee.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorServiceTest {

    private final Path reportsDir = Path.of("target/test-reports");

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(reportsDir)) {
            Files.walk(reportsDir)
                    .sorted((a,b) -> b.compareTo(a))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    void generateCompanyStatisticsPdf_createsPdfFile() throws IOException {
        com.techcorp.employee.service.EmployeeService mockEmp = org.mockito.Mockito.mock(com.techcorp.employee.service.EmployeeService.class);
        // return empty stats map
        org.mockito.Mockito.when(mockEmp.getCompanyStatistics()).thenReturn(java.util.Collections.emptyMap());
        ReportGeneratorService svc = new ReportGeneratorService(mockEmp, reportsDir.toString());
        byte[] pdf = svc.generateCompanyStatisticsPdf("NonExistingCompany");
        assertNotNull(pdf);
        assertTrue(pdf.length > 100, "PDF should contain data");

        // ensure a file was created in reports dir
        Path expected = reportsDir.resolve("statistics_NonExistingCompany.pdf");
        assertTrue(Files.exists(expected), "Report file should be saved to reports directory");
        long size = Files.size(expected);
        assertTrue(size > 0, "Saved report should not be empty");
    }

}
