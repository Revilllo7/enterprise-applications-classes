package com.techcorp.employee.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.springframework.stereotype.Service;

import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.model.Position;

// Import z CSV w trybie strumieniowym - bez wczytywania całego pliku do pamięci

@Service
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

    public ImportSummary importCsv(String csvFilePath) throws IOException {
        Path csvPath = Path.of(csvFilePath);
        return importFromCsv(csvPath, 0);
    }
    
    public ImportSummary importXml(String xmlFilePath) throws IOException {
        Path xmlPath = Path.of(xmlFilePath);
        return importFromXml(xmlPath, 0);
    }

    public ImportSummary importFromXml(Path xmlPath, int maxEntries) throws IOException {
        List<String> errors = new ArrayList<>();
        AtomicInteger imported = new AtomicInteger();
        List<Employee> toImport = new ArrayList<>();
        Set<String> seenEmails = new HashSet<>();

        try (InputStream is = Files.newInputStream(xmlPath)) {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(is);

            AtomicInteger elementNumber = new AtomicInteger();
            boolean inEmployee = false;
            String currentElement = null;

            String firstName = "", lastName = "", email = "", company = "", positionString = "", salaryString = "";

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String name = reader.getLocalName();
                    if ("employee".equalsIgnoreCase(name)) {
                        inEmployee = true;
                        elementNumber.incrementAndGet();
                        firstName = lastName = email = company = positionString = salaryString = "";
                    } else if (inEmployee) {
                        currentElement = name;
                    }
                } else if (event == XMLStreamConstants.CHARACTERS) {
                    if (inEmployee && currentElement != null) {
                        String text = reader.getText();
                        if (text != null) {
                            switch (currentElement.toLowerCase(Locale.ROOT)) {
                                case "firstname":
                                    firstName += text;
                                    break;
                                case "lastname":
                                    lastName += text;
                                    break;
                                case "email":
                                    email += text;
                                    break;
                                case "company":
                                    company += text;
                                    break;
                                case "position":
                                    positionString += text;
                                    break;
                                case "salary":
                                    salaryString += text;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String name = reader.getLocalName();
                    if (inEmployee && "employee".equalsIgnoreCase(name)) {
                        inEmployee = false;
                        int current = elementNumber.get();

                        String fullName = (firstName + " " + lastName).trim();
                        try {
                            double salary;
                            try {
                                salary = Double.parseDouble(salaryString.trim());
                            } catch (NumberFormatException nfe) {
                                errors.add("Employee " + current + ": invalid salary '" + salaryString + "'");
                                continue;
                            }

                            Position position;
                            try {
                                position = Position.valueOf(positionString.trim().toUpperCase(Locale.ROOT));
                            } catch (Exception exception) {
                                errors.add("Employee " + current + ": invalid position '" + positionString + "'");
                                continue;
                            }

                            validateEmployeeData(fullName, email.trim(), company.trim(), position, salary);
                            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
                            if (seenEmails.contains(normalizedEmail)) {
                                errors.add("Employee " + current + ": duplicate email '" + email + "'");
                            } else {
                                Employee employee = new Employee(null, fullName, email.trim(), company.trim(), position, salary);
                                toImport.add(employee);
                                seenEmails.add(normalizedEmail);
                            }
                        } catch (InvalidDataException ide) {
                            errors.add("Employee " + current + ": " + ide.getMessage());
                        } catch (Exception exception) {
                            throw new RuntimeException(exception);
                        }

                        if (maxEntries > 0 && toImport.size() >= maxEntries) break;
                    } else if (inEmployee) {
                        currentElement = null;
                    }
                }
            }
            try { reader.close(); } catch (XMLStreamException ignored) {}
        } catch (XMLStreamException xse) {
            throw new IOException(xse);
        }

        // perform transactional import: clear DB and insert parsed employees
        int count = 0;
        if (!toImport.isEmpty()) {
            count = employeeService.importEmployeesTransactional(toImport);
        }
        return new ImportSummary(count, errors);
    }

    public ImportSummary importFromCsv(Path csvPath, int maxLines) throws IOException {
        List<String> errors = new ArrayList<>();
        AtomicInteger imported = new AtomicInteger();
        List<Employee> toImport = new ArrayList<>();
        Set<String> seenEmails = new HashSet<>();

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

                        // parsowanie stanowiska (case insensitive)
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
                            // walidacja np: ujemna pensja
                            validateEmployeeData(fullName, email, company, position, salary);
                            String normalizedEmail = email.toLowerCase(Locale.ROOT);
                            if (seenEmails.contains(normalizedEmail)) {
                                errors.add("Line " + current + ": duplicate email '" + email + "'");
                                return;
                            }
                            Employee empployee = new Employee(null, fullName, email, company, position, salary);
                            toImport.add(empployee);
                            seenEmails.add(normalizedEmail);
                        } catch (InvalidDataException ide) {
                            errors.add("Line " + current + ": " + ide.getMessage());
                        } catch (Exception exception) {
                            throw new RuntimeException(exception);
                        }
                    });
        }

        int count = 0;
        if (!toImport.isEmpty()) {
            count = employeeService.importEmployeesTransactional(toImport);
        }
        return new ImportSummary(count, errors);
    }

    // walidacja danych pracownika
    void validateEmployeeData(String fullName, String email, String company, Position position, double salary) throws InvalidDataException {
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
