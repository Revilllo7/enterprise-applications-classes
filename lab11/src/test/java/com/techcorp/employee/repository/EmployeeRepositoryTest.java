package com.techcorp.employee.repository;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.dto.EmployeeListView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void projectionListViewReturnsOnlyRequiredColumns() {
        Employee e1 = new Employee(null, "Jan Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee(null, "Anna Nowak", "anna@example.com", "TechCorp", Position.MANAGER, 12000);
        employeeRepository.saveAll(List.of(e1, e2));

        List<EmployeeListView> views = employeeRepository.findAllListView();
        assertEquals(2, views.size());
        EmployeeListView v = views.get(0);
        assertNotNull(v.getFirstName());
        assertNotNull(v.getLastName());
        assertNotNull(v.getPosition());
        // departmentName can be null
    }

    @Test
    void pageableListViewReturnsPageMetadata() {
        for (int i = 0; i < 30; i++) {
            employeeRepository.save(new Employee(null, "User" + i, "u" + i + "@ex.com", "Co", Position.STAZYSTA, 3000));
        }
        Page<EmployeeListView> page = employeeRepository.findAllListView(PageRequest.of(1, 10));
        assertEquals(10, page.getSize());
        assertEquals(3, page.getTotalPages());
        assertEquals(30, page.getTotalElements());
    }

    @Test
    void specificationsFilterByCompanyAndNameAndStatus() {
        Employee e1 = new Employee(null, "Jan Kowalski", "jan@tech.com", "TechCorp", Position.PROGRAMISTA, 8000);
        e1.setStatus(EmploymentStatus.ACTIVE);
        Employee e2 = new Employee(null, "Janina Kowal", "janina@other.com", "OtherCo", Position.MANAGER, 12000);
        e2.setStatus(EmploymentStatus.ON_LEAVE);
        Employee e3 = new Employee(null, "Jan Nowak", "jan.nowak@tech.com", "TechCorp", Position.MANAGER, 15000);
        e3.setStatus(EmploymentStatus.ON_LEAVE);
        employeeRepository.saveAll(List.of(e1, e2, e3));

        Specification<Employee> spec = com.techcorp.employee.specification.EmployeeSpecification
                .byCompany("TechCorp")
                .and(com.techcorp.employee.specification.EmployeeSpecification.nameContains("Jan"))
                .and(com.techcorp.employee.specification.EmployeeSpecification.byStatus(EmploymentStatus.ON_LEAVE));

        List<Employee> filtered = employeeRepository.findAll(spec);
        assertEquals(1, filtered.size());
        assertEquals("jan.nowak@tech.com", filtered.get(0).getEmail());
    }

    @Test
    void specificationsFilterByDepartmentRelationMapping() {
        Department d1 = departmentRepository.save(new Department(null, "IT", "HQ", 1_000_000, "boss@corp.com"));
        Department d2 = departmentRepository.save(new Department(null, "HR", "HQ", 500_000, "hr@corp.com"));

        Employee e1 = new Employee(null, "Ala Alfa", "a@x.com", "Co", Position.STAZYSTA, 3000);
        e1.setDepartment(d1);
        Employee e2 = new Employee(null, "Bar Beta", "b@x.com", "Co", Position.MANAGER, 12000);
        e2.setDepartment(d2);
        Employee e3 = new Employee(null, "Cel Ceti", "c@x.com", "Co", Position.PROGRAMISTA, 9000);
        e3.setDepartment(d1);
        employeeRepository.saveAll(List.of(e1, e2, e3));

        Specification<Employee> byDept = com.techcorp.employee.specification.EmployeeSpecification.byDepartmentId(d1.getId());
        List<Employee> byDepartment = employeeRepository.findAll(byDept);
        assertEquals(2, byDepartment.size());
        assertTrue(byDepartment.stream().allMatch(emp -> d1.getId().equals(emp.getDepartmentId())));
    }
}
