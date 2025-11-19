package com.techcorp.employee.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	// GET /api/employees?company=X
	@GetMapping
	public ResponseEntity<List<EmployeeDTO>> getAll(@RequestParam(name = "company", required = false) String company) {
		List<Employee> employees = employeeService.getAllEmployees();
		List<EmployeeDTO> dtos = employees.stream()
				.filter(e -> company == null || company.isBlank() || (e.getCompanyName() != null && e.getCompanyName().equalsIgnoreCase(company.trim())))
				.map(this::toDto)
				.collect(Collectors.toList());
		return ResponseEntity.ok(dtos);
	}

	// GET /api/employees/{email}
	@GetMapping("/{email}")
	public ResponseEntity<EmployeeDTO> getByEmail(@PathVariable("email") String email) {
		return employeeService.findByEmail(email)
				.map(e -> ResponseEntity.ok(toDto(e)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	// POST /api/employees
	@PostMapping
	public ResponseEntity<EmployeeDTO> create(@RequestBody EmployeeDTO dto) {
		if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()) {
			return ResponseEntity.badRequest().build();
		}

		Employee toCreate = dtoToEmployee(dto, dto.getEmail());
		boolean created = employeeService.addEmployee(toCreate);
		if (!created) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

		// fetch created entity
		Employee createdEmployee = employeeService.findByEmail(dto.getEmail()).orElse(toCreate);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{email}")
				.buildAndExpand(createdEmployee.getEmail())
				.toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(location);
		return new ResponseEntity<>(toDto(createdEmployee), headers, HttpStatus.CREATED);
	}

	// PUT /api/employees/{email}
	@PutMapping("/{email}")
	public ResponseEntity<EmployeeDTO> update(@PathVariable("email") String email, @RequestBody EmployeeDTO dto) {
		return employeeService.findByEmail(email)
				.map(existing -> {
					// create updated Employee preserving identity (email)
					Employee updated = dtoToEmployee(dto, existing.getEmail());
					return employeeService.updateEmployee(email, updated)
							.map(e -> ResponseEntity.ok(toDto(e)))
							.orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
				})
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	// DELETE /api/employees/{email}
	@DeleteMapping("/{email}")
	public ResponseEntity<Void> delete(@PathVariable("email") String email) {
		boolean removed = employeeService.removeEmployee(email);
		if (removed) return ResponseEntity.noContent().build();
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	// PATCH /api/employees/{email}/status
	@PatchMapping("/{email}/status")
	public ResponseEntity<EmployeeDTO> patchStatus(@PathVariable("email") String email, @RequestBody EmployeeDTO dto) {
		if (dto == null || dto.getStatus() == null || dto.getStatus().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		try {
			EmploymentStatus status = EmploymentStatus.valueOf(dto.getStatus().trim().toUpperCase());
			boolean ok = employeeService.updateStatus(email, status);
			if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			return employeeService.findByEmail(email)
					.map(e -> ResponseEntity.ok(toDto(e)))
					.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
		} catch (IllegalArgumentException ex) {
			throw ex; // handled by GlobalExceptionHandler -> 400
		}
	}

	// GET /api/employees/status/{status}
	@GetMapping("/status/{status}")
	public ResponseEntity<List<EmployeeDTO>> getByStatus(@PathVariable("status") String status) {
		try {
			EmploymentStatus st = EmploymentStatus.valueOf(status.trim().toUpperCase());
			List<EmployeeDTO> dtos = employeeService.findByStatus(st).stream().map(this::toDto).collect(Collectors.toList());
			return ResponseEntity.ok(dtos);
		} catch (IllegalArgumentException ex) {
			throw ex; // handled by GlobalExceptionHandler
		}
	}

	// --- mapping helpers ---
    // Unironically I give up, cause at first there was this whole fuss about
    // "What if we have indonesian eployees with single names only"
    // and then code requirements always had first and last name fields.
    // 
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
				position = null; // unknown position -> null
			}
		}
		double salary = dto.getSalary();
		Employee emp = new Employee(fullName, email, company, position, salary);
		if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
			try {
				emp.setStatus(EmploymentStatus.valueOf(dto.getStatus().trim().toUpperCase()));
			} catch (IllegalArgumentException ex) {
				// leave default status if invalid
			}
		}
		return emp;
	}
}
