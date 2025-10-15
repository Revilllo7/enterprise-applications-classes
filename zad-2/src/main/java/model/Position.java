package model;

public enum Position {
    PREZES(25000, 1),
    WICEPREZES(18000, 2),
    MANAGER(12000, 3),
    PROGRAMISTA(8000, 4),
    STAZYSTA(3000, 5);

    private final double baseSalary;
    private final int hierarchyLevel;

    Position(double baseSalary, int hierarchyLevel) {
        this.baseSalary = baseSalary;
        this.hierarchyLevel = hierarchyLevel;
    }

    public double getBaseSalary() { return baseSalary; }
    public int getHierarchyLevel() { return hierarchyLevel; }
}
