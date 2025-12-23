package com.techcorp.employee.controller;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.service.FileStorageService;
import com.techcorp.employee.service.ImportService;
import com.techcorp.employee.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataSize;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.exception.FileNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
 
import com.techcorp.employee.service.ReportGeneratorService;
import com.techcorp.employee.service.EmployeeDocumentService;
import com.techcorp.employee.model.EmployeeDocument;
import java.net.URI;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final ImportService importService;
    private final EmployeeService employeeService;
    private final ReportGeneratorService reportGeneratorService;
    private final EmployeeDocumentService employeeDocumentService;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;
    private static final String[] CSV_ALLOWED = new String[]{"csv"};
    private static final String[] XML_ALLOWED = new String[]{"xml"};

    @Autowired
    public FileUploadController(FileStorageService fileStorageService, ImportService importService, EmployeeService employeeService, ReportGeneratorService reportGeneratorService, EmployeeDocumentService employeeDocumentService) {
        this.fileStorageService = fileStorageService;
        this.importService = importService;
        this.employeeService = employeeService;
        this.reportGeneratorService = reportGeneratorService;
        this.employeeDocumentService = employeeDocumentService;
    }

    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportCsv(@RequestParam(value = "company", required = false) String company) {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            if (company != null && !company.isBlank()) {
                String companyTrim = company.trim();
                employees = employees.stream()
                        .filter(employee -> employee.getCompanyName() != null && employee.getCompanyName().trim().equalsIgnoreCase(companyTrim))
                        .toList();
            }

            StringBuilder stringbuilder = new StringBuilder();
            stringbuilder.append("fullName,email,company,position,salary\n");
            for (Employee employee : employees) {
                stringbuilder.append('"').append(employee.getFullName().replace("\"","\"\"")).append('"').append(',');
                stringbuilder.append(employee.getEmail()).append(',');
                stringbuilder.append('"').append(employee.getCompanyName() == null ? "" : employee.getCompanyName().replace("\"","\"\"")).append('"').append(',');
                stringbuilder.append(employee.getPosition() == null ? "" : employee.getPosition().name()).append(',');
                stringbuilder.append(String.format(java.util.Locale.ROOT, "%.2f", employee.getSalary())).append('\n');
            }

            byte[] bytes = stringbuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ByteArrayResource resource = new ByteArrayResource(Objects.requireNonNull(bytes));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.csv");

            return ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/reports/statistics/{companyName}")
    public ResponseEntity<Resource> statisticsPdf(@PathVariable("companyName") String companyName) {
        try {
            byte[] pdfBytes = reportGeneratorService.generateCompanyStatisticsPdf(companyName);
            ByteArrayResource resource = new ByteArrayResource(Objects.requireNonNull(pdfBytes));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=statistics_" + companyName + ".pdf");

            return ResponseEntity.ok().headers(headers).contentLength(pdfBytes.length).body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/photos/{email}")
    public ResponseEntity<?> uploadPhoto(@PathVariable("email") String email,
                                         @RequestParam("file") MultipartFile file) {
        try {
            // validate employee exists
            var empOpt = employeeService.findByEmail(email);
            if (empOpt.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Employee not found");

            // validate image: jpg/png, max 2MB
            long maxSize = 2L * 1024L * 1024L;
            String[] imgAllowed = new String[]{"jpg", "jpeg", "png"};
            fileStorageService.validateFile(file, maxSize, imgAllowed);

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null) {
                int dot = original.lastIndexOf('.');
                if (dot > -1) ext = original.substring(dot);
            }

            String safeEmail = email.replaceAll("[^A-Za-z0-9@._-]", "_");
            String desired = safeEmail + ext.toLowerCase();

            String relative = fileStorageService.storeFileWithNameInSubDirectory(file, "photos", desired);

            // update employee
            var emp = empOpt.get();
            emp.setPhotoFileName(relative);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(String.format("/api/files/photos/%s", email)));
            return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(emp);

        } catch (InvalidFileException ife) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ife.getMessage());
        } catch (com.techcorp.employee.exception.FileStorageException fse) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(fse.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/photos/{email}")
    public ResponseEntity<Resource> getPhoto(@PathVariable("email") String email) {
        var empOpt = employeeService.findByEmail(email);
        if (empOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        var emp = empOpt.get();
        String photoPath = emp.getPhotoFileName();
        if (photoPath == null || photoPath.isBlank()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        try {
            Resource resource = fileStorageService.loadFileAsResource(photoPath);
            java.nio.file.Path p = fileStorageService.getFilePath(photoPath);
            String contentType = java.nio.file.Files.probeContentType(p);
            if (contentType == null) contentType = "application/octet-stream";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + p.getFileName().toString() + "\"");
            return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength()).body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/documents/{email}")
    public ResponseEntity<?> uploadDocument(@PathVariable("email") String email,
                                            @RequestParam("file") MultipartFile file,
                                            @RequestParam("type") String type) {
        try {
            // Accept type as String to avoid binding/conversion errors. Convert to domain object here.
            com.techcorp.employee.model.DocumentType docType = new com.techcorp.employee.model.DocumentType();
            var doc = employeeDocumentService.storeDocument(email, file, docType);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(String.format("/api/files/documents/%s/%d", email, doc.getId())));
            return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(doc);
        } catch (InvalidDataException ide) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ImportSummary(0, List.of(ide.getMessage())));
        } catch (IOException ioe) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/documents/{email}")
    public ResponseEntity<List<EmployeeDocument>> listDocuments(@PathVariable("email") String email) {
        return ResponseEntity.ok(employeeDocumentService.listDocuments(email));
    }

    @GetMapping("/documents/{email}/{documentId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("email") String email, @PathVariable("documentId") long documentId) {
        try {
            EmployeeDocument doc = employeeDocumentService.getDocument(email, documentId);
            Resource resource = fileStorageService.loadFileAsResource(doc.getFilePath());
            String contentType = java.nio.file.Files.probeContentType(fileStorageService.getFilePath(doc.getFilePath()));
            if (contentType == null) contentType = "application/octet-stream";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getOriginalFileName() + "\"");
            return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength()).body(resource);
        } catch (FileNotFoundException fnf) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException ioe) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/documents/{email}/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable("email") String email, @PathVariable("documentId") long documentId) {
        try {
            employeeDocumentService.deleteDocument(email, documentId);
            return ResponseEntity.noContent().build();
        } catch (FileNotFoundException fnf) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/import/csv")
    public ResponseEntity<ImportSummary> importCsv(@RequestParam("file") MultipartFile file) {
        try {
            fileStorageService.validateFile(file, maxFileSize.toBytes(), CSV_ALLOWED);
            String relative = fileStorageService.storeFileInSubDirectory(file, "imports");
            String fullPath = fileStorageService.getFilePath(relative).toString();
            ImportSummary summary = importService.importCsv(fullPath);
            return ResponseEntity.ok(summary);
        
        } catch (InvalidFileException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ImportSummary(0, List.of(exception.getMessage())));
        
        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ImportSummary(0, List.of(exception.getMessage())));
        
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ImportSummary(0, List.of(exception.getMessage() == null ? exception.toString() : exception.getMessage())));
        }
    }

    @PostMapping("/import/xml")
    public ResponseEntity<ImportSummary> importXml(@RequestParam("file") MultipartFile file) {
        try {
            fileStorageService.validateFile(file, maxFileSize.toBytes(), XML_ALLOWED);
            String relative = fileStorageService.storeFileInSubDirectory(file, "imports");
            String fullPath = fileStorageService.getFilePath(relative).toString();
            ImportSummary summary = importService.importXml(fullPath);
            return ResponseEntity.ok(summary);

        } catch (InvalidFileException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ImportSummary(0, List.of(exception.getMessage())));
        
        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ImportSummary(0, List.of(exception.getMessage())));
        
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ImportSummary(0, List.of(exception.getMessage() == null ? exception.toString() : exception.getMessage())));
        }
    }
}