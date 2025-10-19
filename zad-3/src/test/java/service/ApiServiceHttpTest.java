package service;

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
