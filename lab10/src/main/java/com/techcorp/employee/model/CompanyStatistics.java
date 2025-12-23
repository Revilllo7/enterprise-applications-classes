package com.techcorp.employee.model;

public class CompanyStatistics {
    private final String companyName;
    private final int employeeCount;
    private final double averageSalary;
    private final String highestPaidFullName;

    // New constructor including company name
    public CompanyStatistics(String companyName, int employeeCount, double averageSalary, String highestPaidFullName) {
        this.companyName = companyName;
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.highestPaidFullName = highestPaidFullName;
    }

    // Backwards-compatible constructor (company name unknown)
    public CompanyStatistics(int employeeCount, double averageSalary, String highestPaidFullName) {
        this(null, employeeCount, averageSalary, highestPaidFullName);
    }

    public String getCompanyName() { return companyName; }
    public int getEmployeeCount() { return employeeCount; }
    public double getAverageSalary() { return averageSalary; }
    public String getHighestPaidFullName() { return highestPaidFullName; }

    @Override
    public String toString() {
        return String.format("CompanyStatistics{company=%s, employeeCount=%d, averageSalary=%.2f, highestPaid='%s'}",
                companyName, employeeCount, averageSalary, highestPaidFullName);
    }
}
