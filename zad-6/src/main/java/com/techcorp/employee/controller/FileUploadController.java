// Należy utworzyć kontroler FileUploadController z mapowaniem @RequestMapping("/api/files") obsługujący przesyłanie plików przez API. Kontroler powinien przyjmować pliki używając typu MultipartFile z adnotacją @RequestParam("file"), która automatycznie mapuje przesłany plik z formularza multipart.

// Endpoint POST /api/files/import/csv przyjmuje plik CSV, waliduje jego rozszerzenie i rozmiar, zapisuje go w katalogu uploads, a następnie przekazuje ścieżkę do ImportService który wykonuje import danych. Metoda zwraca obiekt ImportSummary ze szczegółami importu oraz statusem 200 OK przy sukcesie lub odpowiedni kod błędu przy niepowodzeniu.

// Analogiczny endpoint POST /api/files/import/xml obsługuje pliki XML. Oba endpointy powinny zwracać szczegółowe informacje o wyniku importu, włączając liczbę zaimportowanych rekordów, liczbę błędów oraz listę konkretnych błędów z numerami linii lub elementów, które nie zostały przetworzone.

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataSize;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final ImportService importService;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;
    private static final String[] CSV_ALLOWED = new String[]{"csv"};
    private static final String[] XML_ALLOWED = new String[]{"xml"};

    @Autowired
    public FileUploadController(FileStorageService fileStorageService, ImportService importService) {
        this.fileStorageService = fileStorageService;
        this.importService = importService;
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