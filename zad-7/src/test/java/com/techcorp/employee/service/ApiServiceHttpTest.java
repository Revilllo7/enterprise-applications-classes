package com.techcorp.employee.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.techcorp.employee.exception.ApiException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.List;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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


    @Test
    void fetchEmployeesFromApi_parses_json_array_with_mocked_httpclient() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = (HttpResponse<String>) mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[{\"name\": \"John Doe\", \"email\": \"john@example.com\", \"company\": {\"name\": \"TechCorp\"}}]");
        when(mockClient.<String>send(any(HttpRequest.class), org.mockito.Mockito.<HttpResponse.BodyHandler<String>>any())).thenReturn(mockResponse);

        ApiService svc = new ApiService(mockClient);
        List<Employee> list = svc.fetchEmployeesFromApi("http://dummy/json");
        assertEquals(1, list.size());
        Employee e = list.get(0);
        assertEquals("John Doe", e.getFullName());
        assertEquals("TechCorp", e.getCompanyName());
        assertEquals(Position.PROGRAMISTA, e.getPosition());
        assertEquals(Position.PROGRAMISTA.getSalary(), e.getSalary());
    }

    @Test
    void fetchEmployeesFromApi_parses_csv_as_fallback_with_mocked_httpclient() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = (HttpResponse<String>) mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("firstName,lastName,email,company,position,salary\nAnn,Test,ann@example.com,Acme,PROGRAMISTA,8000\n");
        when(mockClient.<String>send(any(HttpRequest.class), org.mockito.Mockito.<HttpResponse.BodyHandler<String>>any())).thenReturn(mockResponse);

        ApiService svc = new ApiService(mockClient);
        List<Employee> list = svc.fetchEmployeesFromApi("http://dummy/csv");
        assertEquals(1, list.size());
        assertEquals("Ann Test", list.get(0).getFullName());
    }

    @Test
    void fetchEmployeesFromApi_throws_on_http_error_with_mocked_httpclient() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = (HttpResponse<String>) mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("oops");
        when(mockClient.<String>send(any(HttpRequest.class), org.mockito.Mockito.<HttpResponse.BodyHandler<String>>any())).thenReturn(mockResponse);

        ApiService svc = new ApiService(mockClient);
        assertThrows(ApiException.class, () -> svc.fetchEmployeesFromApi("http://dummy/bad"));
    }
}
