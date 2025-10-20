
package service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.lang.reflect.Method;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import exception.ApiException;
import model.Employee;
import model.Position;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiServiceHttpTest {

    static Object[][] csvLineCases() {
        return new Object[][]{
            {"a,b,c,d,e,f", 6},
            {"\"a\",b,c,d,e,f", 6},
            {"a,\"b\",c,d,e,f", 6},
            {"a,\"b, c\",d,e,f", 5}, // quoted comma is one field
            {",,,,,", 6},
            {"a,b,c", 3}
        };
    }

    @ParameterizedTest
    @MethodSource("csvLineCases")
    void parseCsvLine_handles_quotes_and_commas(String line, int expectedFields) throws Exception {
        ApiService svc = new ApiService();
        Method m = ApiService.class.getDeclaredMethod("parseCsvLine", String.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<String> fields = (java.util.List<String>) m.invoke(svc, line);
        assertEquals(expectedFields, fields.size());
    }

    @Test
    void safeGetString_handles_missing_null_and_wrong_type() throws Exception {
        ApiService svc = new ApiService();
        Method m = ApiService.class.getDeclaredMethod("safeGetString", JsonObject.class, String.class);
        m.setAccessible(true);
        JsonObject obj = JsonParser.parseString("{\"name\":null}").getAsJsonObject();
        assertEquals("", m.invoke(svc, obj, "name"));
        assertEquals("", m.invoke(svc, obj, "missing"));
        obj.addProperty("num", 123);
        assertEquals("123", m.invoke(svc, obj, "num"));
    }

    // Removed invalid data: URI test. Use local server or valid HTTP/HTTPS URIs for fetchEmployeesFromApi tests.
    private static HttpServer server;
    private static int port;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();

        server.createContext("/json", new Respond("[\n  {\"name\": \"John Doe\", \"email\": \"john@example.com\", \"company\": {\"name\": \"TechCorp\"}}\n]"));
        server.createContext("/csv", new Respond("firstName,lastName,email,company,position,salary\nAnn,Test,ann@example.com,Acme,PROGRAMISTA,8000\n"));
        server.createContext("/bad", new Status(500, "oops"));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void fetchEmployeesFromApi_parses_json_array() throws Exception {
        ApiService svc = new ApiService();
        List<Employee> list = svc.fetchEmployeesFromApi("http://localhost:" + port + "/json");
        assertEquals(1, list.size());
        Employee e = list.get(0);
        assertEquals("John Doe", e.getFullName());
        assertEquals("TechCorp", e.getCompanyName());
        assertEquals(Position.PROGRAMISTA, e.getPosition());
        assertEquals(Position.PROGRAMISTA.getSalary(), e.getSalary());
    }

    @Test
    void fetchEmployeesFromApi_parses_csv_as_fallback() throws Exception {
        ApiService svc = new ApiService();
        List<Employee> list = svc.fetchEmployeesFromApi("http://localhost:" + port + "/csv");
        assertEquals(1, list.size());
        assertEquals("Ann Test", list.get(0).getFullName());
    }

    @Test
    void fetchEmployeesFromApi_throws_on_http_error() {
        ApiService svc = new ApiService();
        assertThrows(ApiException.class, () -> svc.fetchEmployeesFromApi("http://localhost:" + port + "/bad"));
    }

    private record Respond(String body) implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            byte[] data = body.getBytes();
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(data); }
        }
    }

    private record Status(int code, String body) implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            byte[] data = body.getBytes();
            exchange.sendResponseHeaders(code, data.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(data); }
        }
    }
}
