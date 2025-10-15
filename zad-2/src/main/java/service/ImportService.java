package service;

import model.Employee;
import model.Position;
import model.ImportSummary;
import exception.InvalidDataException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

//Import z CSV w trybie strumieniowym — bez wczytywania całego pliku do pamięci.

public class ImportService {
    private final EmployeeService employeeService;

    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Importuje dane z pliku CSV (pomija nagłówek).
     *
     * @param csvPath  ścieżka pliku CSV
     * @param maxLines limit liczby linii do przetworzenia (0 = bez limitu)
     */
    public ImportSummary importFromCsv(Path csvPath, int maxLines) throws IOException { //if you need 2,147,483,648 lines for data, go touch grass
        List<String> errors = new ArrayList<>();
        AtomicInteger imported = new AtomicInteger();

        try (Stream<String> lines = Files.lines(csvPath)) {
            AtomicInteger lineNumber = new AtomicInteger(0);

            Stream<String> processed = lines.skip(1); // pomiń nagłówek
            if (maxLines > 0) processed = processed.limit(maxLines);

            processed
                    .filter(emptyLines -> !emptyLines.trim().isEmpty())
                    .forEach(line -> {
                        int current = lineNumber.incrementAndGet();
                        String[] parts = line.split(",", -1);
                        if (parts.length < 6) {
                            errors.add("Line " + current + ": not enough columns");
                            return;
                        }

                        String firstName = parts[0].trim();
                        String lastName = parts[1].trim();
                        String email = parts[2].trim();
                        String company = parts[3].trim();
                        String positionString = parts[4].trim();
                        String salaryString = parts[5].trim();

                        if (email.isEmpty()) {
                            errors.add("Line " + current + ": missing email");
                            return;
                        }

                        // parse position case-insensitively
                        Position position;
                        try {
                            position = Position.valueOf(positionString.trim().toUpperCase(Locale.ROOT));
                        } catch (Exception exception) {
                            errors.add("Line " + current + ": invalid position '" + positionString + "'");
                            return;
                        }

                        double salary;
                        try {
                            salary = Double.parseDouble(salaryString);
                        } catch (NumberFormatException nfe) {
                            errors.add("Line " + current + ": invalid salary '" + salaryString + "'");
                            return;
                        }

                        String fullName = (firstName + " " + lastName).trim();
                        try {
                            // walidacja np: ujemna pensja, duplikowany email
                            validateEmployeeData(fullName, email, company, position, salary);
                            Employee empployee = new Employee(fullName, email, company, position, salary);
                            boolean added = employeeService.addEmployee(empployee);
                            if (added) imported.incrementAndGet();
                            else errors.add("Line " + current + ": duplicate email '" + email + "'");
                        } catch (InvalidDataException ide) {
                            errors.add("Line " + current + ": " + ide.getMessage());
                        } catch (Exception exception) {
                            throw new RuntimeException(exception);
                        }
                    });
        }

        return new ImportSummary(imported.get(), errors);
    }

    // prosta walidacja pól, może być rozszerzona o dodatkowe reguły
    private void validateEmployeeData(String fullName, String email, String company, Position position, double salary) throws InvalidDataException {
        if (salary < 0) {
            throw new InvalidDataException("invalid salary '" + salary + "'");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new InvalidDataException("invalid email '" + email + "'");
        }
        if (position == null) {
            throw new InvalidDataException("invalid position");
        }
    }
}
