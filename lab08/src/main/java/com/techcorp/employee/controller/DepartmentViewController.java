package com.techcorp.employee.controller;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.DepartmentService;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.FileStorageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/departments")
public class DepartmentViewController {

	private final DepartmentService departmentService;
	private final EmployeeService employeeService;
	private final FileStorageService fileStorageService;

	@Value("${spring.servlet.multipart.max-file-size:10MB}")
	private String maxFileSizeStr;

	private static final String[] DOC_ALLOWED = new String[]{"pdf","doc","docx","txt","png","jpg","jpeg"};

	public DepartmentViewController(DepartmentService departmentService, EmployeeService employeeService, FileStorageService fileStorageService) {
		this.departmentService = departmentService;
		this.employeeService = employeeService;
		this.fileStorageService = fileStorageService;
	}

	@GetMapping
	public String list(Model model) {
		List<Department> departments = departmentService.getAllDepartments();
		List<Employee> all = employeeService.getAllEmployees();
		Map<Long, Long> counts = all.stream()
				.filter(e -> e.getDepartmentId() != null)
				.collect(Collectors.groupingBy(Employee::getDepartmentId, Collectors.counting()));
		Map<Long, String> managerNames = departments.stream()
				.collect(Collectors.toMap(Department::getId,
						d -> {
							if (d.getManagerEmail() == null) return "-";
							return employeeService.findByEmail(d.getManagerEmail()).map(Employee::getFullName).orElse("-");
						}));

		model.addAttribute("departments", departments);
		model.addAttribute("employeeCounts", counts);
		model.addAttribute("managerNames", managerNames);
		return "departments/list";
	}

	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("department", new Department());
		List<Employee> managers = employeeService.getAllEmployees().stream()
				.filter(e -> e.getPosition() != null && e.getPosition().getPosition() <= Position.MANAGER.getPosition())
				.collect(Collectors.toList());
		model.addAttribute("managers", managers);
		return "departments/form";
	}

	@PostMapping
	public String saveDepartment(@Valid @ModelAttribute("department") Department department, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			List<Employee> managers = employeeService.getAllEmployees().stream()
					.filter(e -> e.getPosition() != null && e.getPosition().getPosition() <= Position.MANAGER.getPosition())
					.collect(Collectors.toList());
			model.addAttribute("managers", managers);
			return "departments/form";
		}
		if (department.getId() == null) {
			departmentService.addDepartment(department);
			redirectAttributes.addFlashAttribute("message", "Departament dodany");
		} else {
			departmentService.updateDepartment(department.getId(), department);
			redirectAttributes.addFlashAttribute("message", "Departament zaktualizowany");
		}
		return "redirect:/departments";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
		var dept = departmentService.findById(id).orElse(null);
		if (dept == null) {
			redirectAttributes.addFlashAttribute("error", "Departament nie znaleziony");
			return "redirect:/departments";
		}
		model.addAttribute("department", dept);
		List<Employee> managers = employeeService.getAllEmployees().stream()
				.filter(e -> e.getPosition() != null && e.getPosition().getPosition() <= Position.MANAGER.getPosition())
				.collect(Collectors.toList());
		model.addAttribute("managers", managers);
		return "departments/form";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		boolean removed = departmentService.removeDepartment(id);
		if (removed) redirectAttributes.addFlashAttribute("message", "Departament usunięty");
		else redirectAttributes.addFlashAttribute("error", "Nie znaleziono departamentu");
		return "redirect:/departments";
	}

	@GetMapping("/{id}")
	public String details(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
		var dept = departmentService.findById(id).orElse(null);
		if (dept == null) {
			redirectAttributes.addFlashAttribute("error", "Departament nie znaleziony");
			return "redirect:/departments";
		}
		List<Employee> employees = employeeService.getAllEmployees().stream()
				.filter(e -> e.getDepartmentId() != null && e.getDepartmentId().equals(id))
				.collect(Collectors.toList());
		String managerName = dept.getManagerEmail() == null ? "-" : employeeService.findByEmail(dept.getManagerEmail()).map(Employee::getFullName).orElse("-");
		model.addAttribute("department", dept);
		model.addAttribute("employees", employees);
		model.addAttribute("managerName", managerName);
		return "departments/details";
	}

	// Documents UI for department
	@GetMapping("/documents/{id}")
	public String documents(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
		var dept = departmentService.findById(id).orElse(null);
		if (dept == null) {
			redirectAttributes.addFlashAttribute("error", "Departament nie znaleziony");
			return "redirect:/departments";
		}
		// list files in departments/<id>
		String subdir = "departments/" + id;
		var files = fileStorageService.listFilesInSubDirectory(subdir);
		model.addAttribute("department", dept);
		model.addAttribute("files", files);
		return "departments/documents";
	}

	@PostMapping("/documents/{id}")
	public String uploadDepartmentDocument(@PathVariable("id") Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
										   RedirectAttributes redirectAttributes) {
		var dept = departmentService.findById(id).orElse(null);
		if (dept == null) {
			redirectAttributes.addFlashAttribute("error", "Departament nie znaleziony");
			return "redirect:/departments";
		}
		try {
			long maxBytes = org.springframework.util.unit.DataSize.parse(java.util.Objects.requireNonNull(maxFileSizeStr)).toBytes();
			fileStorageService.validateFile(file, maxBytes, DOC_ALLOWED);
			String relative = fileStorageService.storeFileInSubDirectory(file, "departments/" + id);
			redirectAttributes.addFlashAttribute("message", "Plik przesłany: " + relative);
		} catch (Exception ex) {
			redirectAttributes.addFlashAttribute("error", "Błąd przy przesyłaniu pliku: " + ex.getMessage());
		}
		return "redirect:/departments/documents/" + id;
	}

	@GetMapping("/documents/{id}/download/{fileName}")
	public ResponseEntity<Resource> downloadDepartmentDocument(@PathVariable("id") Long id, @PathVariable("fileName") String fileName) {
		String path = "departments/" + id + "/" + fileName;
		try {
			Resource resource = fileStorageService.loadFileAsResource(path);
			java.nio.file.Path p = fileStorageService.getFilePath(path);
			String contentType = java.nio.file.Files.probeContentType(p);
			if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + p.getFileName().toString() + "\"");
			return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength()).contentType(MediaType.parseMediaType(contentType)).body(resource);
		} catch (Exception ex) {
			return ResponseEntity.notFound().build();
		}
	}

}
