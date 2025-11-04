package com.ppg.iicsdoc.validation.report;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ValidationReportGenerator {

    private final Configuration freemarkerConfig;
    public ValidationReportGenerator(Configuration freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    public enum ReportFormat {
        TEXT("validation-report-text.ftl"),
        MARKDOWN("validation-report-markdown.ftl"),
        HTML("validation-report-html.ftl"),
        JSON("validation-report-json.ftl");

        private final String templatePath;

        ReportFormat(String templatePath) {
            this.templatePath = templatePath;
        }

        public String getTemplatePath() {
            return this.templatePath;
        }
    };

    public String generate(SchemaValidationResult result, ReportFormat format) {
        log.debug("Generating {} validation report", format);

        try {
            Map<String, Object> data = prepareTemplateData(result);
            Template template = freemarkerConfig.getTemplate(format.getTemplatePath());
            StringWriter writer = new StringWriter();

            template.process(data, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Failed to generate {} report", format, e);
            return generateFallbackReport(result);
        }
    }

    public String generateTextReport(SchemaValidationResult result) {
        return generate(result, ReportFormat.TEXT);
    }

    public String generateMarkdownReport(SchemaValidationResult result) {
        return generate(result, ReportFormat.MARKDOWN);
    }

    public String generateHTMLReport(SchemaValidationResult result) {
        return generate(result, ReportFormat.HTML);
    }

    public String generateJSONReport(SchemaValidationResult result) {
        return generate(result, ReportFormat.JSON);
    }

    private Map<String, Object> prepareTemplateData(SchemaValidationResult result) {
        Map<String, Object> data = new HashMap<>();
        data.put("valid", result.isValid());
        data.put("schemaVersion", result.getSchemaVersion());
        data.put("errorCount", result.getErrorCount());
        data.put("warningCount", result.getWarningCount());
        data.put("errors", result.getErrors());
        data.put("warnings", result.getWarnings());

        if (result.getMetrics() != null) {
            data.put("metrics", result.getMetrics());
        }

        return data;
    }

    private String generateFallbackReport(SchemaValidationResult result) {
        StringBuilder report = new StringBuilder();
        report.append("=== Validation Report (Fallback) ===");
        report.append("Status: ").append(result.isValid() ? "Valid" : "Invalid").append("\n");
        report.append("Errors: ").append(result.getErrorCount()).append("\n");
        report.append("Warnings: ").append(result.getWarningCount()).append("\n");

        if (result.hasErrors()) {
            report.append("\nErrors:\n");
            result.getErrors().forEach(error -> report.append("  - [").append(error.getCode())
                    .append("]").append(error.getMessage()).append("\n"));
        }

        if (result.hasWarnings()) {
            report.append("\nWarnings:\n");
            result.getWarnings().forEach(warning -> report.append("  - [").append(warning.getCode())
                    .append("]").append(warning.getMessage()).append("\n"));
        }

        return report.toString();
    }
}
