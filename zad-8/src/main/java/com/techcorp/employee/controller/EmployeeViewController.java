package com.techcorp.employee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.FileStorageService;
import com.techcorp.employee.service.ImportService;
import com.techcorp.employee.model.ImportSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import com.techcorp.employee.dto.EmployeeDTO;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/employees")
public class EmployeeViewController {

	private final EmployeeService employeeService;
	private final FileStorageService fileStorageService;
	private final ImportService importService;

	@Value("${spring.servlet.multipart.max-file-size:10MB}")
	private String maxFileSizeStr;

	private static final String[] CSV_ALLOWED = new String[]{"csv"};
	private static final String[] XML_ALLOWED = new String[]{"xml"};

	@Autowired
	public EmployeeViewController(EmployeeService employeeService, FileStorageService fileStorageService, ImportService importService) {
		this.employeeService = employeeService;
		this.fileStorageService = fileStorageService;
		this.importService = importService;
	}

	@GetMapping
	public String listEmployees(Model model) {
		var dtos = employeeService.getAllEmployees().stream().map(this::toDto).collect(Collectors.toList());
		model.addAttribute("employees", dtos);
		return "employees/list";
	}

	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("employee", new EmployeeDTO());
		model.addAttribute("positions", Position.values());
		model.addAttribute("statuses", EmploymentStatus.values());
		return "employees/add-form";
	}

	@PostMapping("/add")
	public String addEmployee(@Valid @ModelAttribute("employee") EmployeeDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("positions", Position.values());
			model.addAttribute("statuses", EmploymentStatus.values());
			return "employees/add-form";
		}
		Employee toCreate = dtoToEmployee(dto, dto.getEmail());
		boolean created = employeeService.addEmployee(toCreate);
		if (created) {
			redirectAttributes.addFlashAttribute("message", "Pracownik dodany pomyślnie");
		} else {
			redirectAttributes.addFlashAttribute("error", "Nie można dodać pracownika (prawdopodobnie duplikat)");
		}
		return "redirect:/employees";
	}

	@GetMapping("/edit/{email}")
	public String showEditForm(@PathVariable("email") String email, Model model, RedirectAttributes redirectAttributes) {
		Employee employee = employeeService.findByEmail(email).orElse(null);
		if (employee == null) {
			redirectAttributes.addFlashAttribute("error", "Pracownik nie znaleziony");
			return "redirect:/employees";
		}
		model.addAttribute("employee", toDto(employee));
		model.addAttribute("positions", Position.values());
		model.addAttribute("statuses", EmploymentStatus.values());
		return "employees/edit-form";
	}

	@PostMapping("/edit")
	public String editEmployee(@Valid @ModelAttribute("employee") EmployeeDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("positions", Position.values());
			model.addAttribute("statuses", EmploymentStatus.values());
			return "employees/edit-form";
		}
		Employee updated = dtoToEmployee(dto, dto.getEmail());
		var result = employeeService.updateEmployee(dto.getEmail(), updated);
		if (result.isPresent()) {
			redirectAttributes.addFlashAttribute("message", "Dane pracownika zaktualizowane");
		} else {
			redirectAttributes.addFlashAttribute("error", "Nie znaleziono pracownika do aktualizacji");
		}
		return "redirect:/employees";
	}

	@GetMapping("/delete/{email}")
	public String deleteEmployee(@PathVariable("email") String email, RedirectAttributes redirectAttributes) {
		boolean removed = employeeService.removeEmployee(email);
		if (removed) {
			redirectAttributes.addFlashAttribute("message", "Pracownik usunięty");
		} else {
			redirectAttributes.addFlashAttribute("error", "Nie znaleziono pracownika do usunięcia");
		}
		return "redirect:/employees";
	}

	@GetMapping("/search")
	public String showSearchForm() {
		return "employees/search-form";
	}

	@GetMapping("/import")
	public String showImportForm(Model model) {
		model.addAttribute("fileTypes", List.of("csv", "xml"));
		return "employees/import-form";
	}

	@PostMapping("/import")
	public String importFile(@RequestParam("file") MultipartFile file,
							 @RequestParam("fileType") String fileType,
							 RedirectAttributes redirectAttributes) {
		try {
			long maxBytes = org.springframework.util.unit.DataSize.parse(java.util.Objects.requireNonNull(maxFileSizeStr)).toBytes();
			if ("csv".equalsIgnoreCase(fileType)) {
				fileStorageService.validateFile(file, maxBytes, CSV_ALLOWED);
				String relative = fileStorageService.storeFileInSubDirectory(file, "imports");
				String fullPath = fileStorageService.getFilePath(relative).toString();
				ImportSummary summary = importService.importCsv(fullPath);
				redirectAttributes.addFlashAttribute("message", "Import zakończony: " + summary.importedCount() + " rekordów, błędy: " + summary.getFailedCount());
				redirectAttributes.addFlashAttribute("importSummary", summary);
			} else if ("xml".equalsIgnoreCase(fileType)) {
				fileStorageService.validateFile(file, maxBytes, XML_ALLOWED);
				String relative = fileStorageService.storeFileInSubDirectory(file, "imports");
				String fullPath = fileStorageService.getFilePath(relative).toString();
				ImportSummary summary = importService.importXml(fullPath);
				redirectAttributes.addFlashAttribute("message", "Import zakończony: " + summary.importedCount() + " rekordów, błędy: " + summary.getFailedCount());
				redirectAttributes.addFlashAttribute("importSummary", summary);
			} else {
				redirectAttributes.addFlashAttribute("error", "Nieobsługiwany typ pliku: " + fileType);
			}
		} catch (Exception ex) {
			redirectAttributes.addFlashAttribute("error", "Błąd podczas importu: " + ex.getMessage());
		}
		return "redirect:/employees";
	}

	@PostMapping("/search")
	public String searchByCompany(@RequestParam("company") String company, Model model) {
		var matches = employeeService.getAllEmployees().stream()
			.filter(e -> company == null || company.isBlank() || (e.getCompanyName() != null && e.getCompanyName().equalsIgnoreCase(company.trim())))
			.map(this::toDto)
			.collect(Collectors.toList());
		model.addAttribute("employees", matches);
		model.addAttribute("query", company);
		return "employees/search-results";
	}

	// --- helpers ---
	private EmployeeDTO toDto(Employee e) {
		String full = e.getFullName() == null ? "" : e.getFullName().trim();
		String first = "";
		String last = "";
		if (!full.isEmpty()) {
			int idx = full.indexOf(' ');
			if (idx > 0) {
				first = full.substring(0, idx);
				last = full.substring(idx + 1);
			} else {
				first = full;
			}
		}
		EmployeeDTO dto = new EmployeeDTO();
		dto.setFirstName(first);
		dto.setLastName(last);
		dto.setEmail(e.getEmail());
		dto.setCompany(e.getCompanyName());
		Position pos = e.getPosition();
		dto.setPosition(pos == null ? null : pos.name());
		dto.setSalary(e.getSalary());
		dto.setStatus(e.getStatus() == null ? null : e.getStatus().name());
		return dto;
	}

	private Employee dtoToEmployee(EmployeeDTO dto, String emailOverride) {
		String first = dto.getFirstName() == null ? "" : dto.getFirstName().trim();
		String last = dto.getLastName() == null ? "" : dto.getLastName().trim();
		String fullName = (first + (last.isEmpty() ? "" : " "+last)).trim();
		String email = (dto.getEmail() == null || dto.getEmail().isBlank()) ? emailOverride : dto.getEmail();
		String company = dto.getCompany();
		Position position = null;
		if (dto.getPosition() != null && !dto.getPosition().isBlank()) {
			try {
				position = Position.valueOf(dto.getPosition().trim().toUpperCase());
			} catch (IllegalArgumentException ex) {
				position = null;
			}
		}
		double salary = dto.getSalary();
		com.techcorp.employee.model.Employee emp = new Employee(fullName, email, company, position, salary);
		if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
			try {
				emp.setStatus(EmploymentStatus.valueOf(dto.getStatus().trim().toUpperCase()));
			} catch (IllegalArgumentException ex) {
				// ignore
			}
		}
		return emp;
	}

}
