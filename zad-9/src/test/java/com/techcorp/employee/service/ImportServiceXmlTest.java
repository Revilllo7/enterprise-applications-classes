package com.techcorp.employee.service;

import com.techcorp.employee.model.ImportSummary;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ImportServiceXmlTest {

    @Test
    void importFromXml_parsesAndReportsErrors() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<employees>\n" +
                "  <employee>\n" +
                "    <firstName>John</firstName>\n" +
                "    <lastName>Doe</lastName>\n" +
                "    <email>john@example.com</email>\n" +
                "    <company>Acme</company>\n" +
                "    <position>PROGRAMISTA</position>\n" +
                "    <salary>8000</salary>\n" +
                "  </employee>\n" +
                "  <employee>\n" +
                "    <firstName>Jane</firstName>\n" +
                "    <lastName>BadSalary</lastName>\n" +
                "    <email>jane@example.com</email>\n" +
                "    <company>Acme</company>\n" +
                "    <position>PROGRAMISTA</position>\n" +
                "    <salary>notanumber</salary>\n" +
                "  </employee>\n" +
                "  <employee>\n" +
                "    <firstName>Bob</firstName>\n" +
                "    <lastName>BadPosition</lastName>\n" +
                "    <email>bob@example.com</email>\n" +
                "    <company>Acme</company>\n" +
                "    <position>INVALID</position>\n" +
                "    <salary>9000</salary>\n" +
                "  </employee>\n" +
                "  <employee>\n" +
                "    <firstName>John</firstName>\n" +
                "    <lastName>Duplicate</lastName>\n" +
                "    <email>john@example.com</email>\n" +
                "    <company>Acme</company>\n" +
                "    <position>PROGRAMISTA</position>\n" +
                "    <salary>8000</salary>\n" +
                "  </employee>\n" +
                "  <employee>\n" +
                "    <firstName>Alice</firstName>\n" +
                "    <lastName>Negative</lastName>\n" +
                "    <email>alice@example.com</email>\n" +
                "    <company>Acme</company>\n" +
                "    <position>PROGRAMISTA</position>\n" +
                "    <salary>-100</salary>\n" +
                "  </employee>\n" +
                "</employees>\n";

        Path temp = Files.createTempFile("employees", ".xml");
        Files.writeString(temp, xml);

        EmployeeService es = new EmployeeService();
        ImportService svc = new ImportService(es);

        ImportSummary summary = svc.importFromXml(temp, 0);

        // Only the first valid employee should be imported
        assertEquals(1, summary.importedCount());
        assertTrue(summary.getFailedCount() >= 4);
        assertTrue(summary.errors().stream().anyMatch(s -> s.contains("invalid salary")));
        assertTrue(summary.errors().stream().anyMatch(s -> s.contains("invalid position")));
        assertTrue(summary.errors().stream().anyMatch(s -> s.contains("duplicate email") || s.contains("duplicate")));
    }

    @Test
    void importXml_wrapperDelegatesToImportFromXml() throws IOException {
        String xml = "<?xml version=\"1.0\"?><employees></employees>";
        Path temp = Files.createTempFile("emps", ".xml");
        Files.writeString(temp, xml);

        EmployeeService es = new EmployeeService();
        ImportService svc = new ImportService(es);

        ImportSummary summary1 = svc.importFromXml(temp, 0);
        ImportSummary summary2 = svc.importXml(temp.toString());

        assertEquals(summary1.importedCount(), summary2.importedCount());
        assertEquals(summary1.getFailedCount(), summary2.getFailedCount());
    }

    @Test
    void importFromCsv_respectsMaxLinesLimit() throws IOException {
        String csv = "firstName,lastName,email,company,position,salary\n" +
                "A,B,a@a.com,Acme,PROGRAMISTA,1000\n" +
                "C,D,c@c.com,Acme,PROGRAMISTA,2000\n" +
                "E,F,e@e.com,Acme,PROGRAMISTA,3000\n";
        Path temp = Files.createTempFile("emps-csv", ".csv");
        Files.writeString(temp, csv);

        EmployeeService es = new EmployeeService();
        ImportService svc = new ImportService(es);

        ImportSummary summary = svc.importFromCsv(temp, 1); // should import only one
        assertEquals(1, summary.importedCount());
        assertEquals(0, summary.getFailedCount());
    }
}
