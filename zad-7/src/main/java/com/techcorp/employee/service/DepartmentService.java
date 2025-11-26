package com.techcorp.employee.service;

import com.techcorp.employee.model.Department;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DepartmentService {
	private final Map<Long, Department> departments = new HashMap<>();
	private final AtomicLong idSeq = new AtomicLong(1);

	public Department addDepartment(Department department) {
		if (department == null) return null;
		long id = idSeq.getAndIncrement();
		department.setId(id);
		departments.put(id, department);
		return department;
	}

	public List<Department> getAllDepartments() {
		return new ArrayList<>(departments.values());
	}

	public Optional<Department> findById(Long id) {
		if (id == null) return Optional.empty();
		return Optional.ofNullable(departments.get(id));
	}

	public Optional<Department> updateDepartment(Long id, Department updated) {
		if (id == null || updated == null) return Optional.empty();
		Department existing = departments.get(id);
		if (existing == null) return Optional.empty();
		updated.setId(id);
		departments.put(id, updated);
		return Optional.of(updated);
	}

	public boolean removeDepartment(Long id) {
		if (id == null) return false;
		return departments.remove(id) != null;
	}
}
