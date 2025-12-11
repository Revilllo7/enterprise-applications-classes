package com.techcorp.employee.repository;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.dto.EmployeeListView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    void deleteByEmailIgnoreCase(String email);
    List<Employee> findByStatus(EmploymentStatus status);

    // Projection-based list query: fetch only required columns
    @Query("select e.firstName as firstName, e.lastName as lastName, e.position as position, d.name as departmentName from Employee e left join e.department d")
    List<EmployeeListView> findAllListView();

    @Query(value = "select e.firstName as firstName, e.lastName as lastName, e.position as position, d.name as departmentName from Employee e left join e.department d",
           countQuery = "select count(e) from Employee e")
    Page<EmployeeListView> findAllListView(Pageable pageable);
}
 
