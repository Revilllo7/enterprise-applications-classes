package model;

public enum Position {
    PREZES(25000, 1),
    WICEPREZES(18000, 2),
    MANAGER(12000, 3),
    PROGRAMISTA(8000, 4),
    STAZYSTA(3000, 5);

    private final double salary;
    private final int position;

    Position(double baseSalary, int hierarchyLevel) {
        this.salary = baseSalary;
        this.position = hierarchyLevel;
    }

    public double getSalary() { return salary; }

    public int getPosition() {
        return position;
    }
}
