package com.techcorp.employee.dao;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest(properties = "spring.sql.init.mode=never")
public class JdbcEmployeeDAOTest {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private JdbcEmployeeDAO dao;

	@BeforeEach
	void setUp() {
		// create table for tests
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
		assertNotNull(f.getId());
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

	@Test
	void companyStatisticsAggregationAndTopEarner() {
		// company C1: three employees, two with top salary tie
		dao.save(new Employee(null, "John Doe", "john@c1.com", "C1", Position.PROGRAMISTA, 8000));
		dao.save(new Employee(null, "Jane Roe", "jane@c1.com", "C1", Position.MANAGER, 12000));
		dao.save(new Employee(null, "Joe Bloggs", "joe@c1.com", "C1", Position.MANAGER, 12000));

		// employee with null company
		dao.save(new Employee(null, "No Co", "noco@example.com", null, Position.STAZYSTA, 3000));

		List<com.techcorp.employee.model.CompanyStatistics> stats = dao.getCompanyStatistics();
		// find C1 stats
		com.techcorp.employee.model.CompanyStatistics c1 = stats.stream()
				.filter(s -> "C1".equals(s.getCompanyName()))
				.findFirst().orElse(null);
		assertNotNull(c1);
		assertEquals(3, c1.getEmployeeCount());
		// avg = (8000 + 12000 + 12000) / 3 = 10666.666...
		assertEquals((8000 + 12000 + 12000) / 3.0, c1.getAverageSalary(), 0.1);
		// top salary should be 12000 and name should be one of the two top earners
		assertTrue(c1.getHighestPaidFullName().contains("Jane") || c1.getHighestPaidFullName().contains("Joe"));

		// find null-company stats (companyName may be null)
		com.techcorp.employee.model.CompanyStatistics nullCo = stats.stream()
				.filter(s -> s.getCompanyName() == null)
				.findFirst().orElse(null);
		assertNotNull(nullCo);
		assertEquals(1, nullCo.getEmployeeCount());
		assertEquals(3000, nullCo.getAverageSalary(), 0.1);
	}

	@Test
	void invalidEnumValuesMapToDefaults() {
		// insert a row with invalid position and status via direct SQL
		jdbcTemplate.update("INSERT INTO employees(first_name,last_name,email,salary,position,company,status) VALUES (?,?,?,?,?,?,?)",
				"X","Y","bad@example.com",1000.0,"NOT_A_POSITION","Comp","NOT_A_STATUS");
		Optional<Employee> e = dao.findByEmail("bad@example.com");
		assertTrue(e.isPresent());
		assertNull(e.get().getPosition());
		assertEquals(EmploymentStatus.ACTIVE, e.get().getStatus());
	}

	@Test
	void nullFieldsArePersistedAndReadBack() {
		Employee e = new Employee(null, "Null Co", "null@example.com", null, null, 0.0);
		dao.save(e);
		Optional<Employee> found = dao.findByEmail("null@example.com");
		assertTrue(found.isPresent());
		assertNull(found.get().getPosition());
		assertNull(found.get().getCompanyName());
		assertNull(found.get().getPhotoFileName());
		assertNull(found.get().getDepartmentId());
	}

	@Test
	void duplicateEmailThrowsConstraintViolation() {
		dao.save(new Employee(null, "Dup", "dup@example.com", "C", Position.PROGRAMISTA, 5000));
		// second insert with same email should throw DataAccessException
		assertThrows(DataAccessException.class, () -> jdbcTemplate.update(
				"INSERT INTO employees(first_name,last_name,email,salary) VALUES (?,?,?,?)",
				"Dup2","D","dup@example.com",4000.0));
	}

	@Test
	void getCompanyStatisticsEmptyTableReturnsEmptyList() {
		// ensure table empty
		dao.deleteAll();
		List<com.techcorp.employee.model.CompanyStatistics> stats = dao.getCompanyStatistics();
		assertTrue(stats.isEmpty());
	}

	@Test
	void deterministicTopEarnerSelection() {
		dao.save(new Employee(null, "Top One", "t1@co.com", "CoX", Position.PROGRAMISTA, 7000));
		dao.save(new Employee(null, "Top Two", "t2@co.com", "CoX", Position.PREZES, 25000));
		List<com.techcorp.employee.model.CompanyStatistics> stats = dao.getCompanyStatistics();
		com.techcorp.employee.model.CompanyStatistics cs = stats.stream().filter(s -> "CoX".equals(s.getCompanyName())).findFirst().orElse(null);
		assertNotNull(cs);
		assertEquals("Top Two", cs.getHighestPaidFullName());
	}
}

