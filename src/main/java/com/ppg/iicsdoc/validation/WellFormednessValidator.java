package com.ppg.iicsdoc.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.model.validation.ValidationError;

import lombok.extern.slf4j.Slf4j;

/**
 * Validator for checking the well-formedness of XML files.
 * 
 * <p>
 * This validator ensures that an XML file:
 * </p>
 * 
 * <ul>
 * <li>Exists and is not empty</li>
 * <li>Is syntactically correct XML</li>
 * <li>Does not contain parsing errors or fatal issues</li>
 * </ul>
 * 
 * <p>
 * The validation result is returned as a {@link SchemaValidationResult}, which
 * includes a list of {@link ValidationError} objects in case of issues.
 * </p>
 * 
 * <p>
 * Warnings are logged but do not cause the validation to fail. Errors and fatal
 * errors are recorded in the result.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * WellFormednessValidator validator = new WellFormednessValidator();
 * Path xmlFile = Paths.get("example.xml");
 * SchemaValidationResult result = validator.validates(xmlFile);
 * 
 * if (result.isValid()) {
 *     System.out.println("XML is well-formed");
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
public class WellFormednessValidator {

    public enum Mode {
        STRICT,
        LENIENT
    };

    /**
     * Validates that the given XML file is well-formed.
     * 
     * <p>
     * Checks performed include:
     * </p>
     * 
     * <ul>
     * <li>File existence</li>
     * <li>Non-empty content</li>
     * <li>XML syntax parsing</li>
     * </ul>
     * 
     * <p>
     * Errors and fatal errors are captured in the returned
     * {@link SchemaValidationResult}. Warnings are logged but do not affect the
     * validation status.
     * </p>
     * 
     * @param xmlFile the path to the XML file to validate
     * @return {@link SchemaValidationResult} containing validation status, errors,
     *         and warnings.
     */
    public SchemaValidationResult validate(Path xmlFile, Mode mode) {
        log.debug("Checking well-formedness for: {}", xmlFile.getFileName());

        List<ValidationError> errors = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        try {
            if (!Files.exists(xmlFile)) {
                errors.add(ValidationError.fatal("FILE_NOT_FOUND",
                        "XML file does not exist: " + xmlFile));

                return SchemaValidationResult.invalid(errors);
            }

            if (Files.size(xmlFile) == 0) {
                errors.add(ValidationError.error("EMPTY_FILE",
                        "XML file is empty", 0, 0));

                return SchemaValidationResult.invalid(errors);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    log.warn("XML warning: {}", exception.getMessage());
                    if (mode == Mode.STRICT) {
                        errors.add(ValidationError.warning("XML_WARNING",
                                exception.getMessage()));
                    }
                }

                @Override
                public void error(SAXParseException exception) {
                    errors.add(ValidationError.error("XML_ERROR",
                            exception.getMessage(),
                            exception.getLineNumber(),
                            exception.getColumnNumber()));
                }

                @Override
                public void fatalError(SAXParseException exception) {
                    errors.add(ValidationError.fatal("XML_FATAL_ERROR",
                            exception.getMessage()));
                }
            });

            

            builder.parse(xmlFile.toFile());
            if (errors.isEmpty()) {
                log.debug("XML is well-formed");
                return SchemaValidationResult.valid("1.0");
            } else {
                log.warn("XML has well-formedness errors: {}", errors.size());
                return SchemaValidationResult.invalid(errors);
            }
        } catch (SAXException e) {
            log.error("XML parsing error", e);
            errors.add(ValidationError.fatal("PARSE_ERROR",
                    "XML parsing error: " + e.getMessage()));

            return SchemaValidationResult.invalid(errors);
        } catch (IOException e) {
            log.error("IO error", e);
            errors.add(ValidationError.fatal("IO_ERROR",
                    "IO Error: " + e.getMessage()));

            return SchemaValidationResult.invalid(errors);
        } catch (Exception e) {
            log.error("Unexpected error", e);
            errors.add(ValidationError.fatal("UNEXPECTED_ERROR",
                    "Unexpected error: " + e.getMessage()));

            return SchemaValidationResult.invalid(errors);
        }
    }
}
