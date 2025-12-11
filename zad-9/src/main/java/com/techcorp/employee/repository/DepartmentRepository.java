package com.techcorp.employee.repository;

import com.techcorp.employee.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
	Optional<Department> findByNameIgnoreCase(String name);
	boolean existsByNameIgnoreCase(String name);
	Optional<Department> findByManagerEmailIgnoreCase(String managerEmail);
	List<Department> findByLocationIgnoreCase(String location);
	Page<Department> findAllByNameContainingIgnoreCase(String namePart, Pageable pageable);
}
 
