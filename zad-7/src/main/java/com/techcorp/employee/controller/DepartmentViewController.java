package com.techcorp.employee.controller;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.DepartmentService;
import com.techcorp.employee.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/departments")
public class DepartmentViewController {

	private final DepartmentService departmentService;
	private final EmployeeService employeeService;

	public DepartmentViewController(DepartmentService departmentService, EmployeeService employeeService) {
		this.departmentService = departmentService;
		this.employeeService = employeeService;
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
	public String saveDepartment(@ModelAttribute Department department, RedirectAttributes redirectAttributes) {
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
	public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
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
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		boolean removed = departmentService.removeDepartment(id);
		if (removed) redirectAttributes.addFlashAttribute("message", "Departament usunięty");
		else redirectAttributes.addFlashAttribute("error", "Nie znaleziono departamentu");
		return "redirect:/departments";
	}

	@GetMapping("/{id}")
	public String details(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
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

}
