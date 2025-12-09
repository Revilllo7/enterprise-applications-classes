package com.techcorp.employee.specification;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {
    public static Specification<Employee> emailContains(String q) {
        return (root, query, cb) -> (q == null || q.isBlank()) ? cb.conjunction() : cb.like(cb.lower(root.get("email")), "%" + q.toLowerCase() + "%");
    }

    public static Specification<Employee> nameContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            String pat = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pat),
                    cb.like(cb.lower(root.get("lastName")), pat)
            );
        };
    }

    public static Specification<Employee> byCompany(String company) {
        return (root, query, cb) -> (company == null || company.isBlank()) ? cb.conjunction() : cb.equal(cb.lower(root.get("companyName")), company.toLowerCase());
    }

    public static Specification<Employee> byStatus(EmploymentStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Employee> byPosition(Position position) {
        return (root, query, cb) -> position == null ? cb.conjunction() : cb.equal(root.get("position"), position);
    }

    public static Specification<Employee> byDepartmentId(Long departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return cb.conjunction();
            return cb.equal(root.join("department").get("id"), departmentId);
        };
    }
}
