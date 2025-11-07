package com.ppg.iicsdoc.validation.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.validation.report.ValidationReportGenerator.ReportFormat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidationReportExporter {
    
    private final ValidationReportGenerator reportGenerator;
    private static final DateTimeFormatter FILE_TIMESTAMP = 
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public ValidationReportExporter(ValidationReportGenerator generator) {
        this.reportGenerator = generator;
    }

    public Path export(
        SchemaValidationResult result, 
        Path outputDir,
        ReportFormat format) throws IOException {
        log.info("Exporting {} validation report to: {}", format, outputDir);

        Files.createDirectories(outputDir);
        String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP);
        String extension = getFileExtension(format);
        String fileName = "validation-report_%s.%s".formatted(timestamp, extension);
        Path outputFile = outputDir.resolve(fileName);
        String content = reportGenerator.generate(result, format);

        Files.writeString(outputFile, content);
        return outputFile;
    }

    public void exportAll(SchemaValidationResult result, Path outputDir) throws IOException {
        log.info("Exporting validation reports in all formats");
        for (ReportFormat format : ReportFormat.values()) {
            try {
                export(result, outputDir, format);
            } catch (Exception e) {
                log.error("Failed to export {} report", format, e);
            }
        }
    }

    private String getFileExtension(ReportFormat format) {
        return switch (format) {
            case TEXT -> "txt";
            case MARKDOWN -> "md";
            case HTML -> "html";
            case JSON -> "json";
        };
    }
}
