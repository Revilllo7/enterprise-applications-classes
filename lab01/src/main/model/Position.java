package main.model;

public enum Position {
    PREZES(25000, 1),
    WICEPREZES(18000, 2),
    MANAGER(12000, 3),
    PROGRAMISTA(8000, 4),
    STAZYSTA(3000, 5);

    private final double baseSalary;
    private final int Hierarchy;

    Position(double baseSalary, int Hierarchy) {
        this.baseSalary = baseSalary;
        this.Hierarchy = Hierarchy;
    }

    public double getBaseSalary() { return baseSalary; }
    public int getHierarchy() { return Hierarchy; }
}
