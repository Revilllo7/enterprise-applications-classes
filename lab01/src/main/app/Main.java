package main.app;

import main.model.*;
import main.management.repository.EmployeeRepository;
import main.management.*;
import main.management.validation.EmployeeValidation;
import main.analytics.*;
import main.statistics.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        EmployeeRepository repo = new EmployeeRepository();
        EmployeeValidation validator = new EmployeeValidation();
        AddEmployeeToSystem addService = new AddEmployeeToSystem(repo, validator);
        ViewEmployees viewService = new ViewEmployees(repo);


        addService.add(new Employee("Anna Kowalska", "anna@techcorp.com", "TechCorp", Position.PROGRAMISTA, 8500));
        addService.add(new Employee("Karol Bielak", "karol@techcorp.com", "DevHouse", Position.PROGRAMISTA, 9000));
        addService.add(new Employee("Jan Nowak", "jan@techcorp.com", "TechCorp", Position.MANAGER, 12500));
        addService.add(new Employee("Kasia Lis", "kasia@devhouse.com", "DevHouse", Position.STAZYSTA, 3200));
        addService.add(new Employee("Kasia Owca", "kasia@devhouse.com", "DevHouse", Position.STAZYSTA, 3100));

        System.out.println("\n=== All Employees ===");
        viewService.printAll();

        List<Employee> all = viewService.viewAll();

        System.out.println("\n=== Filter by Company: TechCorp ===");
        new FindEmployeeByCompanyName().execute(all, "TechCorp").forEach(System.out::println);

        System.out.println("\n=== Sorted by Last Name ===");
        new SortByLastName().execute(all).forEach(System.out::println);

        System.out.println("\n=== Group by Position ===");
        new GroupByPosition().execute(all).forEach((pos, employees) ->
                System.out.println(pos + " -> " + employees.size() + " employees"));

        System.out.println("\n=== Count by Position ===");
        new CountByPosition().execute(all).forEach((pos, count) ->
                System.out.println(pos + " : " + count));

        System.out.println("\n=== Statistics ===");
        double avg = new CountAverageSalary().execute(all);
        System.out.println("Average salary: " + avg);

        new FindHighestPaidEmployee().execute(all)
                .ifPresent(employee -> System.out.println("Highest paid: " + employee));
    }
}
