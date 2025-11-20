package com.techcorp.employee.controller;

 
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmployeeDocument;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.service.EmployeeDocumentService;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.FileStorageService;
import com.techcorp.employee.service.ImportService;
import com.techcorp.employee.service.ReportGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileUploadController.class)
@TestPropertySource(properties = {"spring.servlet.multipart.max-file-size=10MB"})
public class FileUploadControllerTest {

	@Autowired
	private MockMvc mockMvc;


	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private ImportService importService;

	@Autowired
	private EmployeeService employeeService;

	@Autowired
	private ReportGeneratorService reportGeneratorService;

	@Autowired
	private EmployeeDocumentService employeeDocumentService;

	@Test
	public void uploadCsv_success() throws Exception {
	String csv = "fullName,email,company,position,salary\nJohn Doe,john@example.com,Acme,Dev,1000\n";
	MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csv.getBytes());

	Mockito.doNothing().when(fileStorageService).validateFile(any(org.springframework.web.multipart.MultipartFile.class), org.mockito.ArgumentMatchers.anyLong(), any(String[].class));
	Mockito.when(fileStorageService.storeFileInSubDirectory(any(org.springframework.web.multipart.MultipartFile.class), eq("imports"))).thenReturn("imports/uploaded.csv");
	Mockito.when(fileStorageService.getFilePath(eq("imports/uploaded.csv"))).thenReturn(Paths.get("src/test/resources/uploads/uploaded.csv"));

	ImportSummary summary = new ImportSummary(1, List.of());
	Mockito.when(importService.importCsv(any())).thenReturn(summary);

	mockMvc.perform(multipart("/api/files/import/csv").file(file))
		.andExpect(status().isOk())
		.andExpect(content().contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON)))
		.andExpect(jsonPath("$.importedCount").value(1));
	}

	@Test
	public void uploadTooLarge_returnsPayloadTooLarge() throws Exception {
	// Simulate validation failure for too large file by making validateFile throw
	byte[] big = new byte[1024 * 1024 * 20]; // 20MB
	MockMultipartFile file = new MockMultipartFile("file", "big.csv", "text/csv", big);

	Mockito.doThrow(new com.techcorp.employee.exception.InvalidFileException("File size exceeds the maximum limit"))
		.when(fileStorageService).validateFile(any(org.springframework.web.multipart.MultipartFile.class), org.mockito.ArgumentMatchers.anyLong(), any(String[].class));

	// Controller maps InvalidFileException to 400 Bad Request currently.
	// If the multipart resolver rejects the upload earlier, a 413 would be returned by the server.
	mockMvc.perform(multipart("/api/files/import/csv").file(file))
		.andExpect(status().isBadRequest())
		.andExpect(content().contentTypeCompatibleWith(Objects.requireNonNull(MediaType.APPLICATION_JSON)))
		.andExpect(jsonPath("$.errors[0]").value((org.hamcrest.Matcher<? super String>) containsString("File size exceeds")));
	}

	@Test
	public void uploadInvalidExtension_returnsBadRequest() throws Exception {
	String csv = "not,a,csv\n";
	MockMultipartFile file = new MockMultipartFile("file", "data.txt", "text/plain", csv.getBytes());

	Mockito.doThrow(new com.techcorp.employee.exception.InvalidFileException("File type not allowed: txt"))
		.when(fileStorageService).validateFile(any(org.springframework.web.multipart.MultipartFile.class), org.mockito.ArgumentMatchers.anyLong(), any(String[].class));

	mockMvc.perform(multipart("/api/files/import/csv").file(file))
		.andExpect(status().isBadRequest())
		.andExpect(content().contentTypeCompatibleWith(Objects.requireNonNull(MediaType.APPLICATION_JSON)))
		.andExpect(jsonPath("$.errors[0]").value((org.hamcrest.Matcher<? super String>) containsString("File type not allowed: txt")));
	}

	@Test
	public void downloadCsv_returnsCsvContent() throws Exception {
	// Employee has no default ctor; use available constructor
	Employee e = new Employee("Jane Roe", "jane@example.com", "Acme", com.techcorp.employee.model.Position.PROGRAMISTA, 2000);

	Mockito.when(employeeService.getAllEmployees()).thenReturn(List.of(e));

	mockMvc.perform(get("/api/files/export/csv"))
		.andExpect(status().isOk())
		.andExpect(header().string("Content-Type", (org.hamcrest.Matcher<? super String>) containsString("text/csv")))
		.andExpect(content().string((org.hamcrest.Matcher<? super String>) containsString("fullName,email,company,position,salary")))
		.andExpect(content().string((org.hamcrest.Matcher<? super String>) containsString("jane@example.com")));
	}

	@Test
	public void uploadDocument_createsDocumentAndReturns201() throws Exception {
	byte[] content = "binarydata".getBytes();
	MockMultipartFile file = new MockMultipartFile("file", "idcard.pdf", "application/pdf", content);

	EmployeeDocument doc = new EmployeeDocument(123L, "jan.kowalski@example.com", "documents/jan/idcard_1.pdf", "idcard.pdf", null, Instant.now(), "documents/jan/idcard_1.pdf");
	Mockito.when(employeeDocumentService.storeDocument(eq("jan.kowalski@example.com"), any(org.springframework.web.multipart.MultipartFile.class), any())).thenReturn(doc);

	mockMvc.perform(multipart("/api/files/documents/jan.kowalski@example.com").file(file).param("type", "ID_CARD"))
		.andExpect(status().isCreated())
		.andExpect(header().string("Location", (org.hamcrest.Matcher<? super String>) containsString("/api/files/documents/jan.kowalski@example.com/123")))
		.andExpect(content().contentTypeCompatibleWith(Objects.requireNonNull(MediaType.APPLICATION_JSON)))
		.andExpect(jsonPath("$.id").value(123))
		.andExpect(jsonPath("$.originalFileName").value("idcard.pdf"));
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		public org.springframework.core.convert.converter.Converter<String, com.techcorp.employee.model.DocumentType> documentTypeConverter() {
			return s -> new com.techcorp.employee.model.DocumentType();
		}

		@Bean
		public FileStorageService fileStorageService() {
			return Mockito.mock(FileStorageService.class);
		}

		@Bean
		public ImportService importService() {
			return Mockito.mock(ImportService.class);
		}

		@Bean
		public EmployeeService employeeService() {
			return Mockito.mock(EmployeeService.class);
		}

		@Bean
		public ReportGeneratorService reportGeneratorService() {
			return Mockito.mock(ReportGeneratorService.class);
		}

		@Bean
		public EmployeeDocumentService employeeDocumentService() {
			return Mockito.mock(EmployeeDocumentService.class);
		}
	}

}
