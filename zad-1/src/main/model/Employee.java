package main.model;

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
}
