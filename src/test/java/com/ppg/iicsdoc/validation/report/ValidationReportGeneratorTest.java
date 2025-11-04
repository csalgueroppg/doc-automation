package com.ppg.iicsdoc.validation.report;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.model.validation.ValidationError;
import com.ppg.iicsdoc.model.validation.ValidationWarning;

import freemarker.template.Configuration;
import freemarker.template.Version;

public class ValidationReportGeneratorTest {
    @Autowired
    private ValidationReportGenerator reportGenerator;
    
    private SchemaValidationResult validResult;
    private SchemaValidationResult invalidResult;

    @BeforeEach
    void setUp() {
        validResult = SchemaValidationResult.valid("1.0");

        List<ValidationError> errors = List.of(
            ValidationError.error("TEST_ERROR", "Test error message", 0, 0),
            ValidationError.fatal("FATAL_ERROR", "Fatal test error"));

        List<ValidationWarning> warnings = List.of(
            ValidationWarning.builder()
                .code("TEST_WARNING")
                .message("Test warning message")
                .lineNumber(15)
                .recommendation("Fix this issue")
                .build());
        
        invalidResult = SchemaValidationResult.invalid(errors);
        invalidResult.setWarnings(warnings);

        Configuration config = new Configuration(new Version("2.3.32"));
        this.reportGenerator = new ValidationReportGenerator(config);
    }

    @Test
    void shouldGenerateTextReport() {
        String report = reportGenerator.generateTextReport(invalidResult);

        assertNotNull(report);
        assertTrue(report.contains("Validation Report (Fallback)"));
        assertTrue(report.contains("Invalid"));
        assertTrue(report.contains("TEST_ERROR"));
        assertTrue(report.contains("TEST_WARNING"));
    }

    @Test
    void shouldGenerateMarkdownReport() {
        String report = reportGenerator.generateMarkdownReport(invalidResult);

        assertNotNull(report);
        assertTrue(report.contains("# XML Validation Report"));
        assertTrue(report.contains("## Errors"));
        assertTrue(report.contains("## Warnings"));
        assertTrue(report.contains("TEST_ERROR"));
    }

    @Test
    void shouldGenerateHTMLReport() {
        String report = reportGenerator.generateHTMLReport(invalidResult);

        assertNotNull(report);
        assertTrue(report.contains("<!DOCTYPE html>"));
        assertTrue(report.contains("<title>XML Validation Report</title>"));
        assertTrue(report.contains("TEST_ERROR"));
        assertTrue(report.contains("error-item"));
    }
}
