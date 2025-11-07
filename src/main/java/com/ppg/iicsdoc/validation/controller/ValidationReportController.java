package com.ppg.iicsdoc.validation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.validation.XMLValidationService;
import com.ppg.iicsdoc.validation.report.ValidationReportGenerator;
import com.ppg.iicsdoc.validation.report.ValidationReportGenerator.ReportFormat;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/validation")
public class ValidationReportController {

    private final Configuration freemarkerConfiguration;

    private final XMLValidationService validationService;
    private final ValidationReportGenerator reportGenerator;

    public ValidationReportController(
            XMLValidationService validationService,
            ValidationReportGenerator reportGenerator, Configuration freemarkerConfiguration) {
        this.validationService = validationService;
        this.reportGenerator = reportGenerator;
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    /**
     * Validate XML file and return the report
     */
    @PostMapping("/validate")
    public ResponseEntity<String> validate(
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "json") String format) {
        log.info("Received validation request for file: {}", file.getOriginalFilename());

        try {
            Path tempFile = Files.createTempFile("iics-validation-", ".xml");
            file.transferTo(tempFile);

            SchemaValidationResult result = validationService.validateComplete(tempFile);
            ReportFormat reportFormat = parseFormat(format);
            String report = reportGenerator.generate(result, reportFormat);

            Files.deleteIfExists(tempFile);
            MediaType contentType = getContentType(reportFormat);

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(report);
        } catch (Exception e) {
            log.error("Validation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Validation failed: " + e.getMessage());
        }
    }

    /**
     * Validate XML file and return downloadable content
     */
    @PostMapping("/validate-download")
    public ResponseEntity<byte[]> validateAndDownload(
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "html") String format) {
        log.info("Received validation download request for file: {}", file.getOriginalFilename());

        try {
            Path tempFile = Files.createTempFile("iics-validation-", ".xml");
            file.transferTo(tempFile);

            SchemaValidationResult result = validationService.validateComplete(tempFile);
            ReportFormat reportFormat = parseFormat(format);
            String report = reportGenerator.generate(result, reportFormat);

            Files.deleteIfExists(tempFile);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getContentType(reportFormat));
            headers.setContentDispositionFormData(
                    "attachment",
                    "validation-report." + getFileExtension(reportFormat));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(report.getBytes());
        } catch (Exception e) {
            log.error("Validation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Validation failed: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Get validation report for previously validated files
     */
    @GetMapping("/report/{fileId}")
    public ResponseEntity<String> getReport(
            @PathVariable String fieldId,
            @RequestParam(defaultValue = "html") String format) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body("Report retrieval not yet implemented");
    }

    /**
     * Parse format string to ReportFormat enum
     */
    private ReportFormat parseFormat(String format) {
        return switch (format) {
            case "markdown", "md" -> ReportFormat.MARKDOWN;
            case "html" -> ReportFormat.HTML;
            case "json" -> ReportFormat.JSON;
            case "text", "txt" -> ReportFormat.TEXT;
            default -> ReportFormat.JSON;
        };
    }

    /**
     * Get content type for format
     */
    private MediaType getContentType(ReportFormat format) {
        return switch (format) {
            case MARKDOWN, TEXT -> MediaType.TEXT_PLAIN;
            case HTML -> MediaType.TEXT_HTML;
            case JSON -> MediaType.APPLICATION_JSON;
        };
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
