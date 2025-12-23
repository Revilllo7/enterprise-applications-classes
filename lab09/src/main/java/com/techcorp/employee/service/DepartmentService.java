package com.techcorp.employee.service;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DepartmentService {
	private final DepartmentRepository repository;

	public DepartmentService(DepartmentRepository repository) {
		this.repository = repository;
	}

	public Department addDepartment(Department department) {
		if (department == null) return null;
		return repository.save(department);
	}

	public List<Department> getAllDepartments() {
		return repository.findAll();
	}

	public Optional<Department> findById(Long id) {
		if (id == null) return Optional.empty();
		return repository.findById(id);
	}

	public Optional<Department> updateDepartment(Long id, Department updated) {
		if (id == null || updated == null) return Optional.empty();
		if (!repository.existsById(id)) return Optional.empty();
		updated.setId(id);
		return Optional.of(repository.save(updated));
	}

	public boolean removeDepartment(Long id) {
		if (id == null) return false;
		if (!repository.existsById(id)) return false;
		repository.deleteById(id);
		return true;
	}
}
