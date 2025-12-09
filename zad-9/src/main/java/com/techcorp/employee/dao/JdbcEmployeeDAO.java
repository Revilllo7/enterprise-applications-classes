package com.techcorp.employee.dao;

/**
 * Deprecated placeholder to keep package structure compiling after migration to JPA.
 * All data access is handled by Spring Data repositories now.
 */
@Deprecated
public class JdbcEmployeeDAO {
	public JdbcEmployeeDAO() {}
	public JdbcEmployeeDAO(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {}
	public java.util.List<com.techcorp.employee.model.Employee> findAll() { throw new UnsupportedOperationException("JDBC removed"); }
	public java.util.Optional<com.techcorp.employee.model.Employee> findByEmail(String email) { throw new UnsupportedOperationException("JDBC removed"); }
	public void save(com.techcorp.employee.model.Employee employee) { throw new UnsupportedOperationException("JDBC removed"); }
	public void delete(String email) { throw new UnsupportedOperationException("JDBC removed"); }
	public void deleteAll() { throw new UnsupportedOperationException("JDBC removed"); }
	public java.util.List<com.techcorp.employee.model.CompanyStatistics> getCompanyStatistics() { throw new UnsupportedOperationException("JDBC removed"); }
}
