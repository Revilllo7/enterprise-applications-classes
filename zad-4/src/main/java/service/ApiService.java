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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Pobiera pracowników z zewnętrznego API (jsonplaceholder.typicode.com/users) [10 INDEKSÓW]
 * Mapuje pola:
 *  - name -> fullName (nie dzielimy ze względu na Indonezję </3 )
 *  - email -> email
 *  - company.name -> companyName
 * Dla JSON: wszystkim przypisuje stanowisko PROGRAMISTA i podstawową stawkę Programista.getBaseSalary()
 * Dla CSV: oczekuje wierszy (firstName,lastName,email,companyName,position,salary)
 */
@Service
public class ApiService {
    private final HttpClient httpClient;
    private final Gson gson;
    private final String defaultApiUrl;

    @Autowired
    public ApiService(HttpClient httpClient, Gson gson, @Value("${app.api.url}") String defaultApiUrl) {
        this.httpClient = (httpClient == null) ? HttpClient.newHttpClient() : httpClient;
        this.gson = (gson == null) ? new Gson() : gson;
        this.defaultApiUrl = (defaultApiUrl == null) ? "" : defaultApiUrl;
    }

    // Konstruktor do wstrzykiwania HttpClient (testowanie)
    ApiService(HttpClient httpClient) {
        this.httpClient = (httpClient == null) ? HttpClient.newHttpClient() : httpClient;
        this.gson = new Gson();
        this.defaultApiUrl = "";
    }

    // Konstruktor bezargumentowy na potrzeby testów, NIE używany przez Spring
    ApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.defaultApiUrl = "";
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
        return parseBody(body);
    }

    // Ułatwienie: pobranie z domyślnego adresu z konfiguracji
    public List<Employee> fetchEmployeesFromDefaultApi() throws ApiException {
        if (defaultApiUrl == null || defaultApiUrl.isBlank()) {
            throw new ApiException("Default API URL is not configured");
        }
        return fetchEmployeesFromApi(defaultApiUrl);
    }

    // testowalne bez HTTP
    List<Employee> parseBody(String body) {
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

                Position position = Position.PROGRAMISTA;
                double salary = position.getSalary();
                Employee emp = new Employee(fullName, email, companyName, position, salary);
                result.add(emp);
            }
            return result;
        }

        // CSV parsing - skopiowane z fetchEmployeesFromApi
        String[] lines = body.split("\\r?\\n");
        int index = 0;
        // pomiń puste linie na początku
        while (index < lines.length && lines[index].trim().isEmpty()) index++;

        // wykrycie nagłówka
        if (index < lines.length) {
            String first = lines[index].toLowerCase();
            if (first.contains("email") || first.contains("firstname") || first.contains("firstname".toLowerCase())) {
                index++;
            }
        }

        for (; index < lines.length; index++) {
            String line = lines[index].trim();
            if (line.isEmpty()) continue;
            List<String> fields = parseCsvLine(line);
            if (fields.size() < 6) continue;
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

            double salary = position.getSalary();
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
