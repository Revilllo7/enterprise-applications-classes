package service;

import model.ImportSummary;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import model.Position;
import exception.InvalidDataException;

public class ImportServiceTest {
    @Test
    void validateEmployeeData_catches_all_error_branches() {
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);
        // Negative salary
        InvalidDataException ex1 = assertThrows(InvalidDataException.class, () -> importService.validateEmployeeData("A", "a@b.com", "C", Position.PROGRAMISTA, -1.0));
        assertTrue(ex1.getMessage().contains("invalid salary"));
        // Invalid email
        InvalidDataException ex2 = assertThrows(InvalidDataException.class, () -> importService.validateEmployeeData("A", "", "C", Position.PROGRAMISTA, 1000.0));
        assertTrue(ex2.getMessage().contains("invalid email"));
        InvalidDataException ex3 = assertThrows(InvalidDataException.class, () -> importService.validateEmployeeData("A", "no-at-symbol", "C", Position.PROGRAMISTA, 1000.0));
        assertTrue(ex3.getMessage().contains("invalid email"));
        // Null position
        InvalidDataException ex4 = assertThrows(InvalidDataException.class, () -> importService.validateEmployeeData("A", "a@b.com", "C", null, 1000.0));
        assertTrue(ex4.getMessage().contains("invalid position"));
    }

    @Test
    void importFromCsv_triggers_all_error_paths() throws Exception {
        String csv = "firstName,lastName,email,company,position,salary\n" +
                "A,B,,Acme,PROGRAMISTA,8000\n" + // missing email
                "C,D,c@d.com,Acme,INVALID,9000\n" + // invalid position
                "E,F,e@f.com,Acme,PROGRAMISTA,notanumber\n" + // invalid salary
                "G,H,g@h.com,Acme,PROGRAMISTA,-100\n" + // negative salary
                "I,J,i@j.com,Acme,PROGRAMISTA,8000\n" + // valid
                "I,J,i@j.com,Acme,PROGRAMISTA,8000\n"; // duplicate
        Path temp = Files.createTempFile("employees-errors", ".csv");
        Files.writeString(temp, csv);
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);
        ImportSummary summary = importService.importFromCsv(temp, 0);
        assertEquals(1, summary.importedCount());
        assertTrue(summary.getFailedCount() >= 5);
        assertTrue(summary.errors().stream().anyMatch(e -> e.contains("missing email")));
        assertTrue(summary.errors().stream().anyMatch(e -> e.contains("invalid position")));
        assertTrue(summary.errors().stream().anyMatch(e -> e.contains("invalid salary")));
    assertTrue(summary.errors().stream().anyMatch(e -> e.contains("invalid salary '-100.0'")));
        assertTrue(summary.errors().stream().anyMatch(e -> e.contains("duplicate email")));
    }

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
