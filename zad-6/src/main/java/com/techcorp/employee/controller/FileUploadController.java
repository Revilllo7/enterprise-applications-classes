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
 
import com.techcorp.employee.service.ReportGeneratorService;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final ImportService importService;
    private final EmployeeService employeeService;
    private final ReportGeneratorService reportGeneratorService;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;
    private static final String[] CSV_ALLOWED = new String[]{"csv"};
    private static final String[] XML_ALLOWED = new String[]{"xml"};

    @Autowired
    public FileUploadController(FileStorageService fileStorageService, ImportService importService, EmployeeService employeeService, ReportGeneratorService reportGeneratorService) {
        this.fileStorageService = fileStorageService;
        this.importService = importService;
        this.employeeService = employeeService;
        this.reportGeneratorService = reportGeneratorService;
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

    @PostMapping("/import/csv")
    public ResponseEntity<ImportSummary> importCsv(@RequestParam("file") MultipartFile file) {
        try {
            fileStorageService.validateFile(file, maxFileSize.toBytes(), CSV_ALLOWED);
            String fileName = fileStorageService.storeFile(file);
            String fullPath = fileStorageService.getFilePath(fileName).toString();
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
            String fileName = fileStorageService.storeFile(file);
            String fullPath = fileStorageService.getFilePath(fileName).toString();
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