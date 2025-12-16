package com.techcorp.employee.service;

import com.techcorp.employee.exception.FileNotFoundException;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmployeeDocument;
import com.techcorp.employee.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class EmployeeDocumentServiceTest {

    private FileStorageService fileStorageService;
    private EmployeeService employeeService;
    private EmployeeDocumentService svc;

    @BeforeEach
    void setup() {
        fileStorageService = Mockito.mock(FileStorageService.class);
        employeeService = Mockito.mock(EmployeeService.class);
        svc = new EmployeeDocumentService(fileStorageService, employeeService);
    }

    @Test
    void storeListGetDeleteLifecycle() throws IOException, InvalidDataException {
        String email = "jan@example.com";
        Employee e = new Employee("Jan Kowalski", email, "Acme", com.techcorp.employee.model.Position.PROGRAMISTA, 1000);
        Mockito.when(employeeService.findByEmail(email)).thenReturn(Optional.of(e));

        MockMultipartFile file = new MockMultipartFile("file", "id.pdf", "application/pdf", "x".getBytes());
        Mockito.when(fileStorageService.storeFileInSubDirectory(any(), any())).thenReturn("documents/jan/id_1.pdf");

        EmployeeDocument doc = svc.storeDocument(email, file, new DocumentType());
        assertNotNull(doc);
        assertEquals(email, doc.getEmployeeEmail());
        assertEquals("id.pdf", doc.getOriginalFileName());

        var list = svc.listDocuments(email);
        assertEquals(1, list.size());

        EmployeeDocument loaded = svc.getDocument(email, doc.getId());
        assertEquals(doc.getId(), loaded.getId());

        // delete
        Mockito.doNothing().when(fileStorageService).deleteFile(any());
        svc.deleteDocument(email, doc.getId());

        assertTrue(svc.listDocuments(email).isEmpty());
        assertThrows(FileNotFoundException.class, () -> svc.getDocument(email, doc.getId()));
    }

    @Test
    void storeDocument_missingEmployee_throws() {
        Mockito.when(employeeService.findByEmail("no@one.com")).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "id.pdf", "application/pdf", "x".getBytes());
        assertThrows(InvalidDataException.class, () -> svc.storeDocument("no@one.com", file, new DocumentType()));
    }

}
