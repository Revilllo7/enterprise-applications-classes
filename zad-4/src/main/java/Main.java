import model.Employee;
import model.ImportSummary;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import service.ApiService;
import service.EmployeeService;
import service.ImportService;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@SpringBootApplication(scanBasePackages = {"service", "config"})
public class Main {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class, args);

        EmployeeService employeeService = context.getBean(EmployeeService.class);
        ImportService importService = context.getBean(ImportService.class);
        ApiService apiService = context.getBean(ApiService.class);

        try {
            // --- 1. Import z CSV ---
            Path csvPath = Path.of("src/main/resources/employees.csv");
            ImportSummary summary = importService.importFromCsv(csvPath, 0);
            System.out.println("=== CSV Import ===");
            System.out.println(summary);

            // --- 2. Fetch z API ---
            System.out.println("\n=== Fetch from API ===");
            List<Employee> apiEmployees = apiService.fetchEmployeesFromDefaultApi();
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
