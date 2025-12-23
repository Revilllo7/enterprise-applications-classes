package main.model;

import java.util.Objects;

public class Employee {
    private String fullName;
    private String email;
    private String companyName;
    private Position position;
    private double salary;

    public Employee(String fullName, String email, String companyName, Position position, double salary) {
        this.fullName = fullName;
        this.email = email;
        this.companyName = companyName;
        this.position = position;
        this.salary = salary;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getCompanyName() { return companyName; }
    public Position getPosition() { return position; }
    public double getSalary() { return salary; }


    // Nadpisanie equals(), hashCode() i toString()
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Employee employee)) return false;
        return email.equalsIgnoreCase(employee.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %.2f", fullName, email, companyName, position, salary);
    }
}

