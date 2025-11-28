package com.techcorp.employee.service;

import com.techcorp.employee.model.CompanyStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
 
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ReportGeneratorService {
    private final EmployeeService employeeService;
    private final Path reportsLocation;
    private static final String DEFAULT_REPORTS_DIR = "reports";
    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratorService.class);

    public ReportGeneratorService(EmployeeService employeeService,
                                  @Value("${app.reports.directory:reports}") String reportsDir) {
        this.employeeService = employeeService;
        String dir = (reportsDir == null || reportsDir.isBlank()) ? DEFAULT_REPORTS_DIR : reportsDir;
        this.reportsLocation = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.reportsLocation);
            logger.info("Reports directory initialized at {}", this.reportsLocation);
        } catch (Exception e) {
            throw new RuntimeException("Could not create reports directory: " + this.reportsLocation, e);
        }
    }

    /**
     * Generate a simple PDF report for company statistics and return PDF bytes.
     */
    public byte[] generateCompanyStatisticsPdf(String companyName) throws IOException {
        logger.debug("generateCompanyStatisticsPdf called for company='{}'", companyName);
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
        CompanyStatistics cs = stats.get(companyName == null ? "" : companyName.trim());

        // Prepare text lines
        List<String> lines = new java.util.ArrayList<>();
        lines.add("Company statistics: " + companyName);
        if (cs == null) {
            lines.add("No statistics available for company: " + companyName);
        } else {
            lines.add("Employees: " + cs.getEmployeeCount());
            lines.add(String.format(java.util.Locale.ROOT, "Average salary: %.2f", cs.getAverageSalary()));
            lines.add("Highest earner: " + cs.getHighestPaidFullName());
        }

        // Candidate system font paths
        String[] candidateFontPaths = new String[] {
            // Linux paths
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
            "/usr/share/fonts/truetype/freefont/FreeSans.ttf",
            "/usr/share/fonts/truetype/msttcorefonts/Arial.ttf",
            "/usr/share/fonts/truetype/ubuntu/Ubuntu-R.ttf",
            // windows paths
            "C:\\Windows\\Fonts\\arial.ttf"
        };

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            // Load a TTF font if available and embed it to support Polish characters
            PDFont pdfFont = null;
            for (String fp : candidateFontPaths) {
                java.nio.file.Path p = java.nio.file.Path.of(fp);
                logger.debug("Checking font path: {} (exists: {}, readable: {})", fp, java.nio.file.Files.exists(p), java.nio.file.Files.isReadable(p));
                if (java.nio.file.Files.exists(p) && java.nio.file.Files.isReadable(p)) {
                    try (java.io.InputStream in = java.nio.file.Files.newInputStream(p)) {
                        pdfFont = PDType0Font.load(doc, in);
                        logger.info("Loaded font from {}", fp);
                        break;
                    } catch (Exception e) {
                        logger.warn("Failed to load font from {}: {}", fp, e.toString());
                    }
                } else {
                    logger.debug("Font path not usable: {}", fp);
                }
            }
            logger.debug("Font loading completed.");
            if (pdfFont == null) {
                logger.info("No suitable TTF font loaded from candidates; pdfFont is null — will fall back to image embedding.");
            }

            float marginLeft = 50f;
            float startY = page.getMediaBox().getHeight() - 50f;
            float fontSize = 12f;
            float leading = 1.2f * fontSize;

            if (pdfFont != null) {
                try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                    content.beginText();
                    content.setFont(pdfFont, fontSize + 2);
                    content.newLineAtOffset(marginLeft, startY);
                    // Header
                    content.showText(lines.get(0));
                    content.newLineAtOffset(0, -leading - 4);

                    content.setFont(pdfFont, fontSize);
                    for (int i = 1; i < lines.size(); i++) {
                        content.showText(lines.get(i));
                        if (i < lines.size() - 1) content.newLineAtOffset(0, -leading);
                    }
                    content.endText();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                doc.save(baos);
                byte[] pdfBytes = baos.toByteArray();

                // save generated report to reports directory
                try {
                    String safeName = (companyName == null || companyName.isBlank()) ? "all" : companyName.replaceAll("[^A-Za-z0-9._-]", "_");
                    Path out = this.reportsLocation.resolve("statistics_" + safeName + ".pdf");
                    Files.write(out, pdfBytes);
                    logger.info("Saved report to {}", out);
                } catch (Exception e) {
                    logger.warn("Failed to save report to reports directory: {}", e.toString());
                }

                return pdfBytes;
            } else {

                // IMAGE EMBEDDING FALLBACK
                int width = 500;
                int lineHeight = 18;
                int padding = 20;
                int height = padding * 2 + lines.size() * lineHeight;

                BufferedImage image = new BufferedImage(width, Math.max(height, 100), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                try {
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, width, Math.max(height, 100));
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("SansSerif", Font.BOLD, 14));
                    int y = padding + 14;
                    for (String line : lines) {
                        g.drawString(line, padding, y);
                        y += lineHeight;
                        if (g.getFont().getStyle() != Font.PLAIN) {
                            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        }
                    }
                } finally {
                    g.dispose();
                }

                ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
                javax.imageio.ImageIO.write(image, "PNG", imgOut);
                byte[] imgBytes = imgOut.toByteArray();

                org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject pdImage = org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray(doc, imgBytes, "stats");
                try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                    float imgWidth = pdImage.getWidth();
                    float imgHeight = pdImage.getHeight();
                    float startX = 50;
                    float startYImg = page.getMediaBox().getHeight() - imgHeight - 50;
                    content.drawImage(pdImage, startX, startYImg, imgWidth, imgHeight);
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                doc.save(baos);
                byte[] pdfBytes = baos.toByteArray();

                // save generated report to reports directory (fallback image-based)
                try {
                    String safeName = (companyName == null || companyName.isBlank()) ? "all" : companyName.replaceAll("[^A-Za-z0-9._-]", "_");
                    Path out = this.reportsLocation.resolve("statistics_" + safeName + ".pdf");
                    Files.write(out, pdfBytes);
                    logger.info("Saved report to {}", out);
                } catch (Exception e) {
                    logger.warn("Failed to save report to reports directory: {}", e.toString());
                }

                return pdfBytes;
            }
        }
    }
}

