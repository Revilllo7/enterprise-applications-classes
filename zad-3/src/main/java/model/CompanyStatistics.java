package model;

public class CompanyStatistics {
    private final int employeeCount;
    private final double averageSalary;
    private final String highestPaidFullName;

    public CompanyStatistics(int employeeCount, double averageSalary, String highestPaidFullName) {
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.highestPaidFullName = highestPaidFullName;
    }

    public int getEmployeeCount() { return employeeCount; }
    public double getAverageSalary() { return averageSalary; }
    public String getHighestPaidFullName() { return highestPaidFullName; }

    @Override
    public String toString() {
        return String.format("CompanyStatistics{employeeCount=%d, averageSalary=%.2f, highestPaid='%s'}",
                employeeCount, averageSalary, highestPaidFullName);
    }
}
