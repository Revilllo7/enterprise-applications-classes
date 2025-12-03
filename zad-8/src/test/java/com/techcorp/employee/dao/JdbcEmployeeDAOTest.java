package com.techcorp.employee.dao;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcEmployeeDAOTest {
	private JdbcEmployeeDAO dao;
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		DataSource ds = new DriverManagerDataSource("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
		jdbcTemplate = new JdbcTemplate(ds);
		// create table
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS employees (" +
				"id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
				"first_name VARCHAR(50), " +
				"last_name VARCHAR(50), " +
				"email VARCHAR(100) UNIQUE, " +
				"salary DOUBLE, " +
				"position VARCHAR(100), " +
				"company VARCHAR(100), " +
				"status VARCHAR(20), " +
				"department_id BIGINT, " +
				"photo_file_name VARCHAR(255)" +
				")");
		dao = new JdbcEmployeeDAO(jdbcTemplate);
	}

	@AfterEach
	void tearDown() {
		jdbcTemplate.execute("DROP TABLE IF EXISTS employees");
	}

	@Test
	void insertAndFindByEmail() {
		Employee e = new Employee(null, "M A", "m@example.com", "Co", Position.PROGRAMISTA, 8000);
		dao.save(e);

		Optional<Employee> found = dao.findByEmail("m@example.com");
		assertTrue(found.isPresent());
		Employee f = found.get();
		assertEquals("m@example.com", f.getEmail());
		assertEquals(Position.PROGRAMISTA, f.getPosition());
		assertEquals(8000, f.getSalary());
	}

	@Test
	void updateExistingEmployee() {
		Employee e = new Employee(null, "Upd One", "u@example.com", "Co", Position.STAZYSTA, 3000);
		dao.save(e);
		Optional<Employee> before = dao.findByEmail("u@example.com");
		assertTrue(before.isPresent());

		Employee inserted = before.get();
		Employee updated = new Employee(inserted.getId(), "Upd One New", inserted.getEmail(), "NewCo", Position.MANAGER, 12000);
		dao.save(updated);

		Optional<Employee> after = dao.findByEmail("u@example.com");
		assertTrue(after.isPresent());
		assertEquals("Upd One New", after.get().getFullName());
		assertEquals(Position.MANAGER, after.get().getPosition());
	}

	@Test
	void findAllAndDelete() {
		dao.save(new Employee(null, "A", "a@x.com", "C", Position.PROGRAMISTA, 8000));
		dao.save(new Employee(null, "B", "b@x.com", "C", Position.MANAGER, 12000));

		List<Employee> all = dao.findAll();
		assertEquals(2, all.size());

		dao.delete("a@x.com");
		assertFalse(dao.findByEmail("a@x.com").isPresent());
		assertEquals(1, dao.findAll().size());
	}

	@Test
	void deleteAllClearsTable() {
		dao.save(new Employee(null, "A", "a2@x.com", "C", Position.PROGRAMISTA, 8000));
		dao.save(new Employee(null, "B", "b2@x.com", "C", Position.MANAGER, 12000));
		assertEquals(2, dao.findAll().size());
		dao.deleteAll();
		assertEquals(0, dao.findAll().size());
	}
}

