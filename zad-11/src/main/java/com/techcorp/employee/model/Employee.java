package com.techcorp.employee.model;

import jakarta.persistence.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName = "";

    @Column(name = "last_name", nullable = false)
    private String lastName = "";

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "company")
    String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "position")
    private Position position;

    @Column(name = "salary", nullable = false)
    private double salary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EmploymentStatus status = EmploymentStatus.ACTIVE;

    @Column(name = "photo_file_name")
    private String photoFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    public Employee() {}

    public Employee(Long id, String fullName, String email, String companyName, Position position, double salary) {
        this.id = id;
        setFullName(fullName);
        this.email = email;
        this.companyName = companyName;
        this.position = position;
        this.salary = salary;
        this.status = EmploymentStatus.ACTIVE;
        this.photoFileName = null;
    }

    // Backwards-compatible constructor - sets id to null
    public Employee(String fullName, String email, String companyName, Position position, double salary) {
        this(null, fullName, email, companyName, position, salary);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName == null ? "" : firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName == null ? "" : lastName; }

    public String getFullName() {
        String f = firstName == null ? "" : firstName.trim();
        String l = lastName == null ? "" : lastName.trim();
        return (f + (l.isEmpty() ? "" : " " + l)).trim();
    }
    public void setFullName(String fullName) {
        String fn = fullName == null ? "" : fullName.trim();
        if (fn.isEmpty()) { this.firstName = ""; this.lastName = ""; return; }
        int idx = fn.indexOf(' ');
        if (idx > 0) {
            this.firstName = fn.substring(0, idx);
            this.lastName = fn.substring(idx + 1);
        } else {
            this.firstName = fn;
            this.lastName = "";
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Position getPosition() { return position; }
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public void setPosition(@Nullable Position position) {
        this.position = position;
        if (position == null) {
            this.salary = 0.0;
            return;
        }
        this.salary = position.getSalary();
    }

    public EmploymentStatus getStatus() { return status; }
    public void setStatus(EmploymentStatus status) { this.status = status == null ? EmploymentStatus.ACTIVE : status; }

    public String getPhotoFileName() { return photoFileName; }
    public void setPhotoFileName(String photoFileName) { this.photoFileName = photoFileName; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    // Backward-compatibility for old code using departmentId
    @Transient
    public Long getDepartmentId() { return department == null ? null : department.getId(); }
    public void setDepartmentId(Long departmentId) {
        if (departmentId == null) { this.department = null; }
        else {
            Department d = new Department();
            d.setId(departmentId);
            this.department = d;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Employee)) return false;
        Employee e = (Employee) object;
        return email != null && email.equalsIgnoreCase(e.email);
    }

    @Override
    public int hashCode() { return Objects.hash(email == null ? null : email.toLowerCase()); }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %.2f",
                getFullName(), email, companyName, position, salary);
    }
}
