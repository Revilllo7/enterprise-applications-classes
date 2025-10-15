package model;

import java.util.Objects;

public class Employee {
    private final String fullName; //fullname ze względu na Indonezję </3
    private final String email;
    private final String companyName;
    private final Position position;
    private final double salary;

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
