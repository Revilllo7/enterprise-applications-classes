package com.techcorp.employee.model;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Employee {
    private final String fullName; //fullname ze względu na Indonezję </3
    private final String email;
    String companyName;
    private Position position;
    private double salary;
    private EmploymentStatus status;
    private String photoFileName;
    private Long departmentId;

    public Employee(String fullName, String email, String companyName, Position position, double salary) {
        this.fullName = fullName;
        this.email = email;
        this.companyName = companyName;
        this.position = position;
        this.salary = salary;
        this.status = EmploymentStatus.ACTIVE;
        this.photoFileName = null;
    }

    

    public String getPhotoFileName() { return photoFileName; }

    public void setPhotoFileName(String photoFileName) { this.photoFileName = photoFileName; }

    public EmploymentStatus getStatus() { return status; }

    public void setStatus(EmploymentStatus status) { this.status = status == null ? EmploymentStatus.ACTIVE : status; }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getCompanyName() { return companyName; }
    public Long getDepartmentId() { return departmentId; }
    public Position getPosition() { return position; }
    public double getSalary() { return salary; }

    public void setPosition(@Nullable Position position) {
        this.position = position;
        if (position == null) {
            this.salary = 0.0;
            return;
        }
        this.salary = position.getSalary();
    }

    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Employee)) return false;
        Employee e = (Employee) object;
        return email.equalsIgnoreCase(e.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %.2f",
                fullName, email, companyName, position, salary);
    }
}
