import model.Employee;
import model.ImportSummary;
import service.EmployeeService;
import service.ImportService;
import service.ApiService;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);
        ApiService apiService = new ApiService();

        try {
            // --- 1. Import z CSV ---
            Path csvPath = Path.of("src/main/resources/employees.csv");
            ImportSummary summary = importService.importFromCsv(csvPath, 0);
            System.out.println("=== CSV Import ===");
            System.out.println(summary);

            // --- 2. Fetch z API ---
            System.out.println("\n=== Fetch from API ===");
            List<Employee> apiEmployees = apiService.fetchEmployeesFromApi("https://jsonplaceholder.typicode.com/users");
            apiEmployees.forEach(employeeService::addEmployee);
            System.out.println("Imported from API: " + apiEmployees.size());

            // --- 3. Analiza wynagrodzeń ---
            System.out.println("\n=== Salary consistency check ===");
            employeeService.validateSalaryConsistency().forEach(System.out::println);

            // --- 4. Statystyki firm ---
            System.out.println("\n=== Company statistics ===");
            Map<String, ?> stats = employeeService.getCompanyStatistics();
            stats.forEach((company, stat) -> System.out.println(company + " -> " + stat));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
