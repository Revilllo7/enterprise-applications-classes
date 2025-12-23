package com.techcorp.employee.service;

import com.techcorp.employee.exception.FileNotFoundException;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.DocumentType;
import com.techcorp.employee.model.EmployeeDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EmployeeDocumentService {
    private final FileStorageService fileStorageService;
    private final EmployeeService employeeService;
    private final Map<Long, EmployeeDocument> documents = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> byEmail = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public EmployeeDocumentService(FileStorageService fileStorageService, EmployeeService employeeService) {
        this.fileStorageService = fileStorageService;
        this.employeeService = employeeService;
    }

    public EmployeeDocument storeDocument(String email, MultipartFile file, DocumentType type) throws IOException, InvalidDataException {
        if (email == null || email.isBlank()) throw new InvalidDataException("Employee email missing");
        // validate employee exists
        if (employeeService.findByEmail(email).isEmpty()) {
            throw new InvalidDataException("Employee with email '" + email + "' does not exist");
        }

        // store file under documents/{email}
        String safeEmail = email.replaceAll("[^A-Za-z0-9@._-]", "_");
        String relativePath = fileStorageService.storeFileInSubDirectory(file, "documents/" + safeEmail);

        long id = idGen.getAndIncrement();
        Instant now = Instant.now();
        String original = file.getOriginalFilename();
        String storedFileName = relativePath.contains("/") ? relativePath.substring(relativePath.lastIndexOf('/') + 1) : relativePath;

        EmployeeDocument doc = new EmployeeDocument(id, email, storedFileName, original, type, now, relativePath);
        documents.put(id, doc);
        byEmail.computeIfAbsent(email.toLowerCase(), k -> Collections.synchronizedList(new ArrayList<>())).add(id);
        return doc;
    }

    public List<EmployeeDocument> listDocuments(String email) {
        if (email == null || email.isBlank()) return List.of();
        List<Long> ids = byEmail.get(email.toLowerCase());
        if (ids == null) return List.of();
        List<EmployeeDocument> out = new ArrayList<>();
        for (Long id : ids) {
            EmployeeDocument d = documents.get(id);
            if (d != null) out.add(d);
        }
        return out;
    }

    public EmployeeDocument getDocument(String email, long id) {
        EmployeeDocument d = documents.get(id);
        if (d == null) throw new FileNotFoundException("Document not found: " + id);
        if (!d.getEmployeeEmail().equalsIgnoreCase(email)) throw new FileNotFoundException("Document not found for employee");
        return d;
    }

    public void deleteDocument(String email, long id) {
        EmployeeDocument d = documents.get(id);
        if (d == null) throw new FileNotFoundException("Document not found: " + id);
        if (!d.getEmployeeEmail().equalsIgnoreCase(email)) throw new FileNotFoundException("Document not found for employee");

        // delete file
        fileStorageService.deleteFile(d.getFilePath());

        // remove metadata
        documents.remove(id);
        List<Long> ids = byEmail.get(email.toLowerCase());
        if (ids != null) ids.remove(id);
    }
}
