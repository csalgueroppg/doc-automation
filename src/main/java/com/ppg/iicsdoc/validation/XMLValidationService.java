package com.ppg.iicsdoc.validation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ppg.iicsdoc.metrics.PerformanceMetricsService;
import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.model.validation.ValidationError;
import com.ppg.iicsdoc.model.validation.ValidationMetrics;
import com.ppg.iicsdoc.model.validation.ValidationWarning;
import com.ppg.iicsdoc.validation.cache.ValidationCacheService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating XML files using multiple levels of checks.
 * 
 * <p>
 * This service provides a comprehensive validation workflow, combining:
 * </p>
 * 
 * <ul>
 * <li>Well-formed validation (basic XML syntax check)</li>
 * <li>Schema validation (XSD-based structural checks)</li>
 * <li>Business rules validation (organizational rules beyond schema)</li>
 * </ul>
 * 
 * <p>
 * The service supports both complete validations (all checks) and
 * more focused validations (quick or business-rules-only).
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * XMLValidationService validationService = new XMLValidationService(
 *         schemaValidator,
 *         businessRulesValidator,
 *         wellFormednessValidator);
 * 
 * SchemaValidationResult result = validationService.validateComplete(Paths.get("sample.xml"));
 * if (result.isValid()) {
 *     System.out.println("XML passed all validations!");
 * } else {
 *     result.getErrors().forEach(System.out::println);
 * }
 * }</pre>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-29
 */
@Slf4j
@Service
public class XMLValidationService {

    /** Schema validator */
    private final SchemaValidator schemaValidator;

    /** Business rules validator */
    private final BusinessRulesValidation businessRulesValidator;

    /**  */
    private final WellFormednessValidator wellFormednessValidator;

    /**  */
    private final ValidationCacheService cacheService;

    private PerformanceMetricsService metricsService;

    /**
     * Constructs a new {@code XMLValidationService} with the provided
     * validators.
     * 
     * @param schemaValidator         validator for XSD schema compliance
     * @param businessRulesValidator  validator for custom business rules
     * @param wellFormednessValidator validator for basic XML well-formedness
     */
    public XMLValidationService(
            SchemaValidator schemaValidator,
            BusinessRulesValidation businessRulesValidator,
            WellFormednessValidator wellFormednessValidator,
            ValidationCacheService cacheService) {
        this.schemaValidator = schemaValidator;
        this.businessRulesValidator = businessRulesValidator;
        this.wellFormednessValidator = wellFormednessValidator;
        this.cacheService = cacheService;
        this.metricsService = new PerformanceMetricsService();
    }

    /**
     * Performs a complete validation of the given XML files.
     * 
     * <p>
     * This includes checking:
     * </p>
     * 
     * <ol>
     * <li>Well-formedness</li>
     * <li>Schema validity</li>
     * <li>Business rules compliance</li>
     * </ol>
     * 
     * <p>
     * Results are combined into a single {@link SchemaValidationResult} object.
     * </p>
     * 
     * @param xmlFile the path to the XML file to validate
     * @return a {@code SchemaValidationResult} containing all errors and warnings
     */
    public SchemaValidationResult validateComplete(Path xmlFile) {
        return metricsService.time("validation.complete", () -> {
            SchemaValidationResult cached = cacheService.getCached(xmlFile);
            if (cached != null) {
                return cached;
            }

            log.info("Starting complete validation for: {}", xmlFile.getFileName());
            long startTime = System.currentTimeMillis();
            SchemaValidationResult wellFormednessResult = wellFormednessValidator.validate(
                    xmlFile,
                    WellFormednessValidator.Mode.STRICT);

            if (!wellFormednessResult.isValid()) {
                log.error("XML is not well-formed, skipping further validation");
                return wellFormednessResult;
            }

            SchemaValidationResult schemaResult = schemaValidator.validate(xmlFile);
            if (!schemaResult.isValid()) {
                log.warn("Schema validation failed with {} errors", schemaResult.getErrorCount());
            }

            SchemaValidationResult businessRulesResult = businessRulesValidator.validate(xmlFile);
            SchemaValidationResult combinedResult = combineResults(
                    wellFormednessResult,
                    schemaResult,
                    businessRulesResult);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Complete validation finished in {} ms. Valid: {}, Errors: {}, Warnings: {}",
                    duration,
                    combinedResult.isValid(),
                    combinedResult.getErrorCount(),
                    combinedResult.getWarningCount());

            combinedResult.setMetrics(ValidationMetrics.builder()
                    .validationDurationMs(duration)
                    .wellFormed(true)
                    .build());

            cacheService.cache(xmlFile, combinedResult);
            return combinedResult;
        });
    }

    /**
     * 
     * 
     * @param xmlFile
     * @return
     */
    public SchemaValidationResult validateCompleteNoCache(Path xmlFile) {
        log.info("Starting complete validation for: {}", xmlFile.getFileName());
        long startTime = System.currentTimeMillis();
        SchemaValidationResult wellFormednessResult = wellFormednessValidator.validate(
                xmlFile,
                WellFormednessValidator.Mode.STRICT);

        if (!wellFormednessResult.isValid()) {
            log.error("XML is not well-formed, skipping further validation");
            return wellFormednessResult;
        }

        SchemaValidationResult schemaResult = schemaValidator.validate(xmlFile);
        if (!schemaResult.isValid()) {
            log.warn("Schema validation failed with {} errors", schemaResult.getErrorCount());
        }

        SchemaValidationResult businessRulesResult = businessRulesValidator.validate(xmlFile);
        SchemaValidationResult combinedResult = combineResults(
                wellFormednessResult,
                schemaResult,
                businessRulesResult);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Complete validation finished in {} ms. Valid: {}, Errors: {}, Warnings: {}",
                duration,
                combinedResult.isValid(),
                combinedResult.getErrorCount(),
                combinedResult.getWarningCount());

        combinedResult.setMetrics(ValidationMetrics.builder()
                .validationDurationMs(duration)
                .wellFormed(true)
                .build());

        return combinedResult;
    }

    /**
     * Performs a quick validation of the XML file.
     * 
     * <p>
     * Checks only well-formedness and, if valid, schema compliance.
     * Business rules are not checked.
     * </p>
     * 
     * @param xmlFile the XML file to validate
     * @return {@link SchemaValidationResult} of the quick validation
     */
    public SchemaValidationResult validateQuick(Path xmlFile) {
        log.info("Starting quick validation for: {}", xmlFile.getFileName());

        SchemaValidationResult wellFormednessResult = wellFormednessValidator.validate(xmlFile,
                WellFormednessValidator.Mode.STRICT);
        if (wellFormednessResult.isValid()) {
            return wellFormednessResult;
        }

        return schemaValidator.validate(xmlFile);
    }

    /**
     * Validates only the business rules for the given XML file.
     *
     * @param xmlFile the XML file to validate
     * @return {@link SchemaValidationResult} containing business rules validation
     *         results
     */
    public SchemaValidationResult validateBusinessRules(Path xmlFile) {
        return businessRulesValidator.validate(xmlFile);
    }

    /**
     * Combines multiple {@link SchemaValidationResult} objects into a single result
     * object.
     * 
     * <p>
     * Aggregates all errors and warnings. The combined result is considered valid
     * only if all input results are valid.
     * </p>
     * 
     * @param results one or more {@link SchemaValidationResult} objects to combine
     * @return a single {@code SchemaValidationResult} representing the aggregate
     *         outcome.
     */
    private SchemaValidationResult combineResults(
            SchemaValidationResult... results) {
        List<ValidationError> allErrors = new ArrayList<>();
        List<ValidationWarning> allWarnings = new ArrayList<>();

        boolean isValid = true;
        for (SchemaValidationResult result : results) {
            if (!result.isValid()) {
                isValid = false;
            }

            if (result.getErrors() != null) {
                allErrors.addAll(result.getErrors());
            }

            if (result.getWarnings() != null) {
                allWarnings.addAll(result.getWarnings());
            }
        }

        SchemaValidationResult combined = isValid
                ? SchemaValidationResult.valid("1.0")
                : SchemaValidationResult.invalid(allErrors);

        combined.setWarnings(allWarnings);
        return combined;
    }
}
