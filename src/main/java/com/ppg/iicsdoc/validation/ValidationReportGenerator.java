package com.ppg.iicsdoc.validation;

import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.model.validation.ValidationError;
import com.ppg.iicsdoc.model.validation.ValidationWarning;

import lombok.extern.slf4j.Slf4j;

/**
 * Generates textual, Markdown, and HTML reports for XML validation results.
 * 
 * <p>
 * This class converts a {@link SchemaValidationResult} into human-readable
 * reports in multiple formats. It supports:
 * </p>
 * 
 * <ul>
 * <li>Plain text reports for console or log output</li>
 * <li>Markdown reports suitable for GitHub, documentation, or emails</li>
 * <li>HTML reports for browser display or dashboards</li>
 * </ul>
 * 
 * <p>
 * Reports include summary information, validation metrics, and detailed
 * errors and warnings. Errors and warnings include line/column information,
 * severity, messages, and optional suggestions or recommendations.
 * </p>
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * ValidationReportGenerator generator = new ValidationReportGenerator();
 * SchemaValidationResult result = xmlValidationService.validateComplete(path);
 * 
 * String textReport = generator.generateTextReport(result);
 * String markdownReport = generator.generateMarkdownReport(result);
 * String htmlReport = generator.generateHTMLReport(result);
 * }</pre>
 * </p>
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-29
 */
@Slf4j
@Component
public class ValidationReportGenerator {

    /**
     * Generates a plain-text report from the given validation result.
     * 
     * <p>
     * The report contains:
     * </p>
     * 
     * <ul>
     * <li>Validation status (VALID/INVALID)</li>
     * <li>Schema version</li>
     * <li>Metrics such as duration, file size, well-formed, schema valid</li>
     * <li>Detailed errors and warnings with optional
     * suggestions/recommendations</li>
     * </ul>
     *
     * @param result the validation result to report
     * @return a formatted text report
     */
    public String generateTextReport(SchemaValidationResult result) {
        StringBuilder report = new StringBuilder();

        report.append("==============================================\n");
        report.append("  XML Validation Report\n");
        report.append("==============================================\n");
        report.append("Status: ")
                .append(result.isValid() ? "VALID" : "INVALID")
                .append("\n");

        if (result.getSchemaVersion() != null) {
            report.append("Schema Version: ")
                    .append(result.getSchemaVersion())
                    .append("\n");
        }

        report.append("\n");
        if (result.getMetrics() != null) {
            report.append("Metrics:\n");
            report.append(" - Validation Duration: ")
                    .append(result.getMetrics().getValidationDurationMs())
                    .append(" ms\n");

            report.append(" - File Size: ")
                    .append(result.getMetrics().getFileSizeBytes())
                    .append(" bytes\n");

            report.append(" - Well-formed: ")
                    .append(result.getMetrics().isWellFormed() ? "Yes" : "No")
                    .append("\n");

            report.append(" - Schema Valid: ")
                    .append(result.getMetrics().isSchemaValid() ? "Yes" : "No")
                    .append("\n");

            report.append("\n");
        }

        if (result.hasErrors()) {
            report.append("ERRORS (").append(result.getErrorCount()).append("):\n");
            report.append("--------------------------------------------------------\n");

            for (ValidationError error : result.getErrors()) {
                report.append(" ").append(getSeverityIcon(error.getSeverity()))
                        .append(" [").append(error.getCode()).append("] ");

                if (error.getLineNumber() > 0) {
                    report.append("Line ").append(error.getLineNumber()).append(": ");
                }

                report.append(error.getMessage()).append("\n");
                if (error.getSuggestion() != null) {
                    report.append("    Suggestion: ").append(error.getSuggestion()).append("\n");
                }

                report.append("\n");
            }
        }

        if (result.hasWarnings()) {
            report.append("WARNINGS (").append(result.getWarningCount()).append("):\n");
            report.append("--------------------------------------------------------\n");

            for (ValidationWarning warning : result.getWarnings()) {
                report.append("  [").append(warning.getCode()).append("] ");

                if (warning.getLineNumber() > 0) {
                    report.append("Line ").append(warning.getLineNumber()).append(": ");
                }

                report.append(warning.getMessage()).append("\n");
                if (warning.getRecommendation() != null) {
                    report.append("    Recommendation: ")
                            .append(warning.getRecommendation())
                            .append("\n");
                }

                report.append("\n");
            }
        }

        report.append("==============================================\n");
        return report.toString();
    }

    /**
     * Generates a Markdown-formatted report from the given validation result.
     * 
     * <p>
     * This report is suitable for:
     * </p>
     * 
     * <ul>
     * <li>Documentation systems</li>
     * <li>Markdown viewers (GitHub, GitLab)</li>
     * <li>Email reports</li>
     * </ul>
     * 
     * <p>
     * The report includes summary, metrics, errors, warnings, and optional
     * suggestions/recommendations.
     * </p>
     *
     * @param result the validation result to report
     * @return a formatted Markdown report
     */
    public String generateMarkdownReport(SchemaValidationResult result) {
        StringBuilder report = new StringBuilder();

        report.append("# XML Validation Report\n\n");
        report.append("## Summary \n\n");
        report.append("- **Status**: ")
                .append(result.isValid() ? "Valid" : "Invalid")
                .append("\n");

        if (result.getSchemaVersion() != null) {
            report.append("- **Schema Version**: ")
                    .append(result.getSchemaVersion())
                    .append('\n');
        }

        report.append("- **Errors**: ")
                .append(result.getErrorCount())
                .append("\n");
        report.append("- **Warnings**: ")
                .append(result.getWarningCount())
                .append("\n\n");

        if (result.getMetrics() != null) {
            report.append("## Metrics:\n\n");
            report.append(" - **Validation Duration**: ")
                    .append(result.getMetrics().getValidationDurationMs())
                    .append(" ms\n");

            report.append(" - **File Size**: ")
                    .append(result.getMetrics().getFileSizeBytes())
                    .append(" bytes\n");

            report.append(" - **Well-formed**: ")
                    .append(result.getMetrics().isWellFormed() ? "Yes" : "No")
                    .append("\n");

            report.append(" - **Schema Valid**: ")
                    .append(result.getMetrics().isSchemaValid() ? "Yes" : "No")
                    .append("\n\n");
        }

        if (result.hasErrors()) {
            report.append("## Errors \n\n");

            for (ValidationError error : result.getErrors()) {
                report.append("### ").append(error.getSeverity()).append(": ")
                        .append(error.getCode()).append("\n\n");

                if (error.getLineNumber() > 0) {
                    report.append("**Location**: Line ").append(error.getLineNumber());
                    if (error.getColumnNumber() > 0) {
                        report.append(", Column ").append(error.getColumnNumber());
                    }

                    report.append("\n\n");
                }

                report.append("**Message**: ")
                        .append(error.getMessage())
                        .append("\n\n");

                if (error.getSuggestion() != null) {
                    report.append("**Suggestion**: ")
                            .append(error.getSuggestion())
                            .append("\n\n");
                }
            }
        }

        if (result.hasWarnings()) {
            report.append("## Warnings\n\n");

            for (ValidationWarning warning : result.getWarnings()) {
                report.append("### ").append(warning.getCode()).append("\n\n");
                report.append("**Message**: ").append(warning.getMessage()).append("\n\n");

                if (warning.getRecommendation() != null) {
                    report.append("**Recommendation**: ")
                            .append(warning.getRecommendation())
                            .append("\n\n");
                }
            }
        }

        return report.toString();
    }

    /**
     * Generates an HTML report from the given validation result.
     * <p>
     * The HTML report includes:
     * </p>
     * <ul>
     * <li>Summary section with validation status, error count, and warning
     * count</li>
     * <li>Errors section with line numbers, messages, and severity</li>
     * <li>Warnings section with messages and recommendations</li>
     * <li>Basic styling for readability (colors for valid/invalid, errors, and
     * warnings)</li>
     * </ul>
     *
     * @param result the validation result to report
     * @return a formatted HTML report as a string
     */
    public String generateHTMLReport(SchemaValidationResult result) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<title>XML Validation Report</title>\n");
        html.append("style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append(".valid { color: green; }\n");
        html.append(".invalid { color: red; }\n");
        html.append(
                ".error { background-color: #ffebee; padding: 10px; margin: 10px 0; border-left: 4px solid #f44336; }\n");
        html.append(
                ".warning { background-color: #fff3e0; padding: 10px; margin: 10px 0; border-left: 4px solid #ff9800; }\n");
        html.append("</style>\n</head>\n<body>\n");

        html.append("<h1>XML Validation Report</h1>\n");
        html.append("<h2>Summary</h2>\n");
        html.append("p class=\"").append(result.isValid() ? "valid" : "invalid").append("\">");
        html.append("Status: <strong>").append(result.isValid() ? "VALID" : "INVALID").append("</strong></p>\n");
        html.append("<p>Errors: ").append(result.getErrorCount()).append("</p>\n");
        html.append("<p>Warnings: ").append(result.getWarningCount()).append("</p>\n");

        if (result.hasErrors()) {
            html.append("<h2>Errors</h2>\n");
            for (ValidationError error : result.getErrors()) {
                html.append("<div class=\"error\">\n");
                html.append("<strong>[").append(error.getCode()).append("]</strong> ");

                if (error.getLineNumber() > 0) {
                    html.append("Line ").append(error.getLineNumber()).append(": ");
                }

                html.append(error.getMessage());
                html.append("</div>\n");
            }
        }

        if (result.hasWarnings()) {
            html.append("<h2>Warnings</h2>\n");
            for (ValidationWarning warning : result.getWarnings()) {
                html.append("<div class=\"warning\">\n");
                html.append("<strong>[").append(warning.getCode()).append("]</strong> ");
                html.append(warning.getMessage());
                html.append("</div>\n");
            }
        }

        html.append("</body>\n</html>");
        return html.toString();
    }

    /**
     * Returns a symbol representing the severity of a validation error.
     *
     * <p>
     * Mapping:
     * </p>
     * <ul>
     * <li>FATAL: "x"</li>
     * <li>ERROR: "x"</li>
     * <li>WARNING: "" (no icon)</li>
     * <li>INFO: "i"</li>
     * </ul>
     *
     * @param severity the severity level of the error
     * @return a string representing the severity icon
     */
    private String getSeverityIcon(ValidationError.ErrorSeverity severity) {
        return switch (severity) {
            case FATAL -> "x";
            case ERROR -> "x";
            case WARNING -> "";
            case INFO -> "i";
        };
    }
}
