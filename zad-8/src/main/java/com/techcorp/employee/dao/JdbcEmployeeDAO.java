package com.techcorp.employee.dao;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcEmployeeDAO implements EmployeeDAO {
	private final JdbcTemplate jdbcTemplate;

	public JdbcEmployeeDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private static final RowMapper<Employee> EMPLOYEE_ROW_MAPPER = new RowMapper<>() {
		@Override
		public Employee mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			Long id = resultSet.getObject("id") == null ? null : resultSet.getLong("id");
			String firstName = resultSet.getString("first_name");
			String lastName = resultSet.getString("last_name");
			String fullName;
			if (firstName == null) firstName = "";
			if (lastName == null || lastName.isBlank()) fullName = firstName.trim();
			else fullName = (firstName + " " + lastName).trim();

			String email = resultSet.getString("email");
			double salary = resultSet.getDouble("salary");
			String positionStr = resultSet.getString("position");
			Position position = null;
			if (positionStr != null && !positionStr.isBlank()) {
				try { position = Position.valueOf(positionStr); } catch (IllegalArgumentException ignored) { position = null; }
			}
			String company = resultSet.getString("company");
			String statusStr = resultSet.getString("status");
			EmploymentStatus status = EmploymentStatus.ACTIVE;
			if (statusStr != null && !statusStr.isBlank()) {
				try { status = EmploymentStatus.valueOf(statusStr); } catch (IllegalArgumentException ignored) { status = EmploymentStatus.ACTIVE; }
			}
			Long departmentId = resultSet.getObject("department_id") == null ? null : resultSet.getLong("department_id");
			String photo = resultSet.getString("photo_file_name");

			Employee employee = new Employee(id, fullName, email, company, position, salary);
			employee.setStatus(status);
			employee.setDepartmentId(departmentId);
			employee.setPhotoFileName(photo);
			return employee;
		}
	};

	@Override
	public List<Employee> findAll() {
		String sql = "SELECT id, first_name, last_name, email, salary, position, company, status, department_id, photo_file_name FROM employees";
		return jdbcTemplate.query(sql, EMPLOYEE_ROW_MAPPER);
	}

	@Override
	public Optional<Employee> findByEmail(String email) {
		if (email == null || email.isBlank()) return Optional.empty();
		String sql = "SELECT id, first_name, last_name, email, salary, position, company, status, department_id, photo_file_name FROM employees WHERE email = ?";
		try {
			Employee e = jdbcTemplate.queryForObject(sql, EMPLOYEE_ROW_MAPPER, email);
			return Optional.ofNullable(e);
		} catch (EmptyResultDataAccessException ex) {
			return Optional.empty();
		}
	}

	@Override
	public void save(Employee employee) {
		if (employee == null) return;
		// split fullName to first_name and last_name
		String fullName = employee.getFullName();
		String firstName = "";
		String lastName = "";
		if (fullName != null && !fullName.isBlank()) {
			String[] parts = fullName.trim().split("\\s+", 2);
			firstName = parts[0];
			if (parts.length > 1) lastName = parts[1];
		}

		String email = employee.getEmail();
		Double salary = employee.getSalary();
		String position = employee.getPosition() == null ? null : employee.getPosition().name();
		String company = employee.getCompanyName();
		String status = employee.getStatus() == null ? EmploymentStatus.ACTIVE.name() : employee.getStatus().name();
		Long departmentId = employee.getDepartmentId();
		String photo = employee.getPhotoFileName();

		if (employee.getId() == null) {
			String sql = "INSERT INTO employees (first_name, last_name, email, salary, position, company, status, department_id, photo_file_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			KeyHolder keyHolder = new GeneratedKeyHolder();
			final String firstNameFinal = firstName;
			final String lastNameFinal = lastName;
			final String emailFinal = email;
			final Double salaryFinal = salary;
			final String positionFinal = position;
			final String companyFinal = company;
			final String statusFinal = status;
			final Long departmentIdFinal = departmentId;
			final String photoFinal = photo;

			jdbcTemplate.update(connection -> {
				PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				preparedStatement.setString(1, firstNameFinal);
				preparedStatement.setString(2, lastNameFinal);
				preparedStatement.setString(3, emailFinal);
				preparedStatement.setDouble(4, salaryFinal);
				if (positionFinal != null) preparedStatement.setString(5, positionFinal); else preparedStatement.setNull(5, java.sql.Types.VARCHAR);
				if (companyFinal != null) preparedStatement.setString(6, companyFinal); else preparedStatement.setNull(6, java.sql.Types.VARCHAR);
				preparedStatement.setString(7, statusFinal);
				if (departmentIdFinal != null) preparedStatement.setLong(8, departmentIdFinal); else preparedStatement.setNull(8, java.sql.Types.BIGINT);
				if (photoFinal != null) preparedStatement.setString(9, photoFinal); else preparedStatement.setNull(9, java.sql.Types.VARCHAR);
				return preparedStatement;
			}, keyHolder);
			// generated id is available in keyHolder if caller needs it
		} else {
			String sql = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, salary = ?, position = ?, company = ?, status = ?, department_id = ?, photo_file_name = ? WHERE id = ?";
			jdbcTemplate.update(sql,
					firstName,
					lastName,
					email,
					salary,
					position,
					company,
					status,
					departmentId,
					photo,
					employee.getId());
		}
	}

	@Override
	public void delete(String email) {
		if (email == null || email.isBlank()) return;
		String sql = "DELETE FROM employees WHERE email = ?";
		jdbcTemplate.update(sql, email);
	}

	@Override
	public void deleteAll() {
		jdbcTemplate.update("DELETE FROM employees");
	}

	@Override
	public List<com.techcorp.employee.model.CompanyStatistics> getCompanyStatistics() {
		String sql = "SELECT e.company AS company, COUNT(*) AS cnt, AVG(e.salary) AS avg_salary, MAX(e.salary) AS max_salary, " +
				"(SELECT CONCAT(x.first_name, ' ', x.last_name) FROM employees x WHERE x.company = e.company ORDER BY x.salary DESC LIMIT 1) AS top_name " +
				"FROM employees e GROUP BY e.company";

		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			String company = rs.getString("company");
			int count = rs.getInt("cnt");
			double avg = rs.getDouble("avg_salary");
			String top = rs.getString("top_name");
			if (top == null) top = "";
			return new com.techcorp.employee.model.CompanyStatistics(company, count, avg, top);
		});
	}
}
