package com.techcorp.employee.dao;

import com.techcorp.employee.model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeDAO {
	List<Employee> findAll();

	Optional<Employee> findByEmail(String email);

	void save(Employee employee);

	void delete(String email);

	void deleteAll();

	java.util.List<com.techcorp.employee.model.CompanyStatistics> getCompanyStatistics();
}
