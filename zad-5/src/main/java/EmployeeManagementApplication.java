import model.Employee;
import model.ImportSummary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import service.ApiService;
import service.EmployeeService;
import service.ImportService;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@SpringBootApplication(scanBasePackages = {"service", "config"})
@ImportResource("classpath:employees-beans.xml")
public class EmployeeManagementApplication implements CommandLineRunner {
    private final EmployeeService employeeService;
    private final ImportService importService;
    private final ApiService apiService;
    private final List<Employee> xmlEmployees;
    private final String csvFileName;

    public EmployeeManagementApplication(
            EmployeeService employeeService,
            ImportService importService,
            ApiService apiService,
            @Qualifier("xmlEmployees") List<Employee> xmlEmployees,
            @Value("${app.import.csv-file}") String csvFileName
    ) {
        this.employeeService = employeeService;
        this.importService = importService;
        this.apiService = apiService;
        this.xmlEmployees = xmlEmployees;
        this.csvFileName = csvFileName;
    }

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
    // Import from CSV (path from application.properties, loaded from classpath)
        System.out.println("=== CSV Import ===");
    Path csvPath = new ClassPathResource(csvFileName).getFile().toPath();
        ImportSummary summary = importService.importFromCsv(csvPath, 0);
        System.out.println(summary);

        // Add employees provided via XML bean list
        System.out.println("\n=== Adding employees from XML bean ===");
        xmlEmployees.forEach(employeeService::addEmployee);
        System.out.println("Added from XML: " + xmlEmployees.size());

        // Fetch employees from REST API (URL from properties injected into ApiService)
        System.out.println("\n=== Fetch from API ===");
        List<Employee> apiEmployees = apiService.fetchEmployeesFromDefaultApi();
        apiEmployees.forEach(employeeService::addEmployee);
        System.out.println("Imported from API: " + apiEmployees.size());

        // Company statistics (example for 'TechCorp')
        System.out.println("\n=== Company statistics (TechCorp) ===");
        Map<String, ?> stats = employeeService.getCompanyStatistics();
        if (stats.containsKey("TechCorp")) {
            System.out.println("TechCorp -> " + stats.get("TechCorp"));
        } else if (!stats.isEmpty()) {
            // print first available entry
            Map.Entry<String, ?> first = stats.entrySet().iterator().next();
            System.out.println(first.getKey() + " -> " + first.getValue());
        } else {
            System.out.println("No company stats available.");
        }

        // Salary consistency validation
        System.out.println("\n=== Underpaid employees (actual < base) ===");
        employeeService.salaryConsistencyReport().forEach(System.out::println);
    }
}
