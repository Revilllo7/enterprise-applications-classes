package service;

import model.Employee;
import model.Position;
import exception.ApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;
import com.google.gson.JsonObject;

/**
 * Pobiera pracowników z zewnętrznego API (jsonplaceholder.typicode.com/users) [10 INDEKSÓW]
 * Mapuje pola:
 *  - name -> fullName (nie dzielimy ze względu na Indonezję </3 )
 *  - email -> email
 *  - company.name -> companyName
 * Dla JSON: wszystkim przypisuje stanowisko PROGRAMISTA i podstawową stawkę Programista.getBaseSalary()
 * Dla CSV: oczekuje wierszy (firstName,lastName,email,companyName,position,salary)
 */
public class ApiService {
    private final HttpClient httpClient;

    public ApiService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Wykonuje GET i mapuje dane. Rzuca ApiException w przypadku błędów HTTP lub parsowania.
     * Obsługuje JSON array (stare API) oraz CSV (firstName,lastName,email,companyName,position,salary).
     */
    public List<Employee> fetchEmployeesFromApi(String url) throws ApiException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("HTTP request failed", e);
        }

        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new ApiException("Unexpected HTTP status: " + status);
        }

        String body = response.body();
        if (body == null) body = "";

        // Najpierw spróbuj JSON array
        JsonElement root = null;
        try {
            root = JsonParser.parseString(body);
        } catch (JsonSyntaxException ignored) {
            // jeśli nieprawidłowy to się nie wywalaj
        }

        List<Employee> result = new ArrayList<>();

        if (root != null && root.isJsonArray()) {
            JsonArray array = root.getAsJsonArray();
            for (JsonElement element : array) {
                if (!element.isJsonObject()) continue;
                JsonObject object = element.getAsJsonObject();

                String fullName = safeGetString(object, "name"); // używamy jako fullname
                String email = safeGetString(object, "email");

                String companyName = "";
                if (object.has("company") && object.get("company").isJsonObject()) {
                    companyName = safeGetString(object.getAsJsonObject("company"), "name");
                }

                // przypisujemy PROGRAMISTA i bazową stawkę
                Position pos = Position.PROGRAMISTA;
                double salary = pos.getBaseSalary();

                Employee emp = new Employee(fullName, email, companyName, pos, salary);
                result.add(emp);
            }
            return result;
        }

        // Jeżeli to nie był JSON, traktujemy jako CSV
        String[] lines = body.split("\\r?\\n");
        int index = 0;
        // pomiń puste linie na początku
        while (index < lines.length && lines[index].trim().isEmpty()) index++;

        // wykrycie nagłówka
        if (index < lines.length) {
            String first = lines[index].toLowerCase();
            if (first.contains("email") || first.contains("firstname") || first.contains("firstName".toLowerCase())) {
                index++;
            }
        }

        for (; index < lines.length; index++) {
            String line = lines[index].trim();
            if (line.isEmpty()) continue;
            List<String> fields = parseCsvLine(line);
            if (fields.size() < 6) {
                // pomijamy niekompletne wiersze
                continue;
            }
            String firstName = fields.get(0).trim();
            String lastName = fields.get(1).trim();
            String email = fields.get(2).trim();
            String companyName = fields.get(3).trim();
            String positionString = fields.get(4).trim();
            String salaryString = fields.get(5).trim();

            String fullName = firstName;
            if (!lastName.isEmpty()) fullName = firstName + " " + lastName;

            Position position = Position.PROGRAMISTA;
            if (!positionString.isEmpty()) {
                try {
                    position = Position.valueOf(positionString.trim().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    // jeżeli nie można sparsować - zostawiamy domyślne PROGRAMISTA
                }
            }

            double salary = position.getBaseSalary();
            if (!salaryString.isEmpty()) {
                try {
                    salary = Double.parseDouble(salaryString.replace(',', '.'));
                } catch (NumberFormatException ignored) {
                }
            }

            Employee employee = new Employee(fullName, email, companyName, position, salary);
            result.add(employee);
        }

        return result;
    }

    private String safeGetString(JsonObject object, String member) {
        if (!object.has(member) || object.get(member).isJsonNull()) return "";
        try {
            return object.get(member).getAsString();
        } catch (ClassCastException | IllegalStateException exception) {
            return "";
        }
    }

    // csv parser
    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // znak ucieczki - podwójny cudzysłów
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (character == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
