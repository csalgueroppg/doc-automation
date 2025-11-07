package com.ppg.iicsdoc.validation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.model.validation.ValidationError;
import com.ppg.iicsdoc.model.validation.ValidationMetrics;

import lombok.extern.slf4j.Slf4j;

/**
 * Validates XML files against a predefined XML Schema Definition (XSD).
 * 
 * <p>
 * This class provides functionality to validate XML files against a schema
 * loaded from the classpath. By default, it loads the schema located at
 * {@link #DEFAULT_SCHEMA}.
 * </p>
 * 
 * <p>
 * The validation produces detailed information about warnings, errors, and
 * fatal errors using the nested {@link ValidationErrorHandler} class.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>{@code
 * SchemaValidator validator = new SchemaValidator();
 * SchemaValidationResult result = validator.validate(Paths.get("example.xml"));
 * 
 * if (result.isSchemaValid()) {
 *     System.out.println("XML is valid!");
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
@Component
public class SchemaValidator {

    /** Default schema path if none is explicitly provided */
    private static final String DEFAULT_SCHEMA = "/schemas/iics-process-complete.xsd";

    /**
     * The {@link Schema} instance loaded from classpath that will be used for
     * validation
     */
    private final Schema schema;

    /**
     * Creates a new {@code SchemaValidator} instance and loads the default schema
     * located at {@link #DEFAULT_SCHEMA}.
     * 
     * <P>
     * If the schema cannot be found or fails to load, an
     * {@link IllegalStateException} is thrown.
     * </p>
     */
    public SchemaValidator() {
        this.schema = loadSchema(DEFAULT_SCHEMA);
    }

    /**
     * Creates a new {@code SchemaValidator} instance and loads the schema file
     * from the provided file path.
     * 
     * @param schemaPath path where the schema is located
     */
    public SchemaValidator(String schemaPath) {
        this.schema = loadSchema(schemaPath);
    }

    // Public API
    /**
     * Validates the specified XML file against the loaded XSD schema.
     * 
     * <p>
     * This method checks whether the XML file exists and is readable, then
     * validates it against the schema loaded by {@link #loadSchema(String)}.
     * Validation errors are collected and returned in a
     * {@link SchemaValidationResult}.
     * </p>
     * 
     * @param xmlFile the path to the XML file to validate
     * @return a {@link SchemaValidationResult} containing validation
     *         success/failures and any errors encountered.
     * @throws IllegalStateException if a fatal schema validation error occurs
     */
    public SchemaValidationResult validate(Path xmlFile) {
        log.info("Validating XML file against schema: {}", xmlFile.getFileName());

        long startTime = System.currentTimeMillis();
        List<ValidationError> errors = new ArrayList<>();

        try {
            if (!Files.exists(xmlFile)) {
                errors.add(ValidationError.fatal("FILE_NOT_FOUND",
                        "XML file does not exist: " + xmlFile));

                return SchemaValidationResult.invalid(errors);
            }

            if (!Files.isReadable(xmlFile)) {
                errors.add(ValidationError.fatal("FILE_NOT_READABLE",
                        "XML file is not readable: " + xmlFile));

                return SchemaValidationResult.invalid(errors);
            }

            long fileSize = Files.size(xmlFile);
            Validator validator = schema.newValidator();
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();

            validator.setErrorHandler(errorHandler);
            try (InputStream is = Files.newInputStream(xmlFile)) {
                validator.validate(new StreamSource(is));
            }

            errors.addAll(errorHandler.getErrors());

            long duration = System.currentTimeMillis() - startTime;
            ValidationMetrics metrics = ValidationMetrics.builder()
                    .validationDurationMs(duration)
                    .fileSizeBytes(fileSize)
                    .wellFormed(errors.stream().noneMatch(e -> e.getSeverity() == ValidationError.ErrorSeverity.FATAL))
                    .schemaValid(errors.isEmpty())
                    .build();

            if (errors.isEmpty()) {
                log.info("Schema validation passed in {} ms", duration);

                SchemaValidationResult result = SchemaValidationResult.valid("1.0");
                result.setMetrics(metrics);

                return result;
            } else {
                log.warn("Schema validation failed with {} errors", errors.size());

                SchemaValidationResult result = SchemaValidationResult.invalid(errors);
                result.setMetrics(metrics);

                return result;
            }
        } catch (SAXException e) {
            log.error("Schema validation error", e);
            errors.add(ValidationError.fatal("SCHEMA_ERROR",
                    "Schema validation error: " + e.getMessage()));

            return SchemaValidationResult.invalid(errors);
        } catch (IOException e) {
            log.error("IO error during validation", e);
            errors.add(ValidationError.fatal("IO_ERROR",
                    "IO error: " + e.getMessage()));

            return SchemaValidationResult.invalid(errors);
        }
    }

    /**
     * Load an XSD schema file from the classpath and returns a {@link Schema}
     * instance.
     * 
     * <p>
     * The schema is loaded using a {@link SchemaFactory} with secure processing
     * enabled. If the schema file cannot be found or is invalid, this method
     * throws an {@link IllegalStateException}.
     * </p>
     * 
     * @param schemaPath the classpath location of the XSD schema file (e.g.,
     *                   "/schema/process.xsd")
     * @return a {@link Schema} instance representing the loaded XSD
     * @throws IllegalStateException if the schema file cannot be found or fails to
     *                               parse
     */
    private Schema loadSchema(String schemaPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            URL schemaUrl = getClass().getResource(schemaPath);
            if (schemaUrl == null) {
                throw new IllegalStateException("Schema file not found: " + schemaPath);
            }

            StreamSource source = new StreamSource(schemaUrl.openStream());
            source.setSystemId(schemaUrl.toExternalForm());

            log.info("Loaded schema from {} (targetNamespace: probably OK)", schemaPath);
            return factory.newSchema(source);
        } catch (SAXException e) {
            log.error("Failed to load schema", e);
            throw new IllegalStateException("Failed to load schema: " + schemaPath, e);
        } catch (IOException e) {
            log.error("StreamSource I/O operations failed", e);
            throw new IllegalStateException("I/O operations failed", e);
        }
    }

    /**
     * Custom {@link ErrorHandler} implementation for capturing XML schema
     * validation.
     * 
     * <p>
     * This handler collecs warnings, errors, and fatal errors encountered during
     * validation and stores them as a list of {@link ValidationError} instances. It
     * also logs each issue using the configured logger.
     * </p>
     * 
     * <p>
     * The severity of each validation problem is mapped as follows:
     * </p>
     * <ul>
     * <li>{@link #warning(SAXParseException)} ->
     * {@link ValidationError.ErrorSeverity#WARNING}</li>
     * <li>{@link #error(SAXParseException)} ->
     * {@link ValidationError.ErrorSeverity#ERROR}</li>
     * <li>{@link #fatal(SAXParseException)} ->
     * {@link ValidationError.ErrorSeverity#FATAL}</li>
     * </ul>
     * 
     * @version 1.0.0
     * @since 2025-10-28
     */
    private static class ValidationErrorHandler implements ErrorHandler {
        private final List<ValidationError> errors = new ArrayList<>();

        /**
         * Handles a non-fatal warning encountered during XML validation.
         * 
         * @param exception the {@link SAXParseException} describing the warning
         */
        @Override
        public void warning(SAXParseException exception) {
            log.warn("Validation warning: {}", exception.getMessage());
            errors.add(ValidationError.builder()
                    .severity(ValidationError.ErrorSeverity.WARNING)
                    .code("SCHEMA_WARNING")
                    .message(exception.getMessage())
                    .lineNumber(exception.getLineNumber())
                    .columnNumber(exception.getColumnNumber())
                    .build());
        }

        /**
         * Handles a recoverable error encountered during XML validation.
         *
         * @param exception the {@link SAXParseException} describing the error
         */
        @Override
        public void error(SAXParseException exception) {
            log.error("Validation error: {}", exception.getMessage());
            errors.add(ValidationError.builder()
                    .severity(ValidationError.ErrorSeverity.ERROR)
                    .code("SCHEMA_eRROR")
                    .message(exception.getMessage())
                    .lineNumber(exception.getLineNumber())
                    .columnNumber(exception.getColumnNumber())
                    .build());
        }

        /**
         * Handles a fatal error encountered during XML validation.
         * 
         * <p>
         * This indicates that validation cannot continue.
         * </p>
         *
         * @param exception the {@link SAXParseException} describing the fatal error
         */
        @Override
        public void fatalError(SAXParseException exception) {
            log.error("Validation fatal error: {}", exception.getMessage());
            errors.add(ValidationError.builder()
                    .severity(ValidationError.ErrorSeverity.FATAL)
                    .code("SCHEMA_FATAL_ERROR")
                    .message(exception.getMessage())
                    .lineNumber(exception.getLineNumber())
                    .columnNumber(exception.getColumnNumber())
                    .build());
        }

        /**
         * Returns the list of validation errors collected by this handler.
         * 
         * @return a {@link List} of {@link ValidationError} objects, in the
         *         order they were encountered.
         */
        public List<ValidationError> getErrors() {
            return errors;
        }
    }
}
