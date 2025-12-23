package service;

import model.ImportSummary;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ImportServiceTest {

    @Test
    void importFromCsv_shouldImportValidAndSkipInvalid() throws IOException {
        String csv = """
                firstName,lastName,email,company,position,salary
                Alice,Doe,alice@example.com,TechCorp,Manager,12000
                Bob,Smith,bob@example.com,TechCorp,Programista,9000
                ,NoEmail,,TechCorp,Manager,10000
                Eve,Zero,eve@example.com,TechCorp,Unknown,5000
                """;
        Path temp = Files.createTempFile("employees", ".csv");
        Files.writeString(temp, csv);

        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);

        ImportSummary summary = importService.importFromCsv(temp, 0);

        assertEquals(2, summary.importedCount());
        assertEquals(2, summary.errors().size());
        assertEquals(2, employeeService.getAllEmployees().size());
    }

    @Test
    void importFromClasspath_resource_parses_expected_counts() throws IOException {
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);
        ImportSummary summary = importService.importFromCsv(Path.of("src/test/resources/employees.csv"), 2048);

        // test CSV ma 5 poprawnych i 3 błędne wiersze
        assertEquals(2, summary.importedCount(), "Expected 2 successfully imported employees");
        assertEquals(3, summary.getFailedCount(), "Expected 3 failed rows");
        assertTrue(summary.errors().size() >= 3, "Expected error messages for failed rows");
    }
}
