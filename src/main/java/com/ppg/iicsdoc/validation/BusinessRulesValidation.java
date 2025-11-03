package com.ppg.iicsdoc.validation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.model.validation.ValidationError;
import com.ppg.iicsdoc.model.validation.ValidationWarning;

import lombok.extern.slf4j.Slf4j;

/**
 * Validates business rules for XML-based process definitions.
 * 
 * <p>
 * This class checks that an XML process definition adheres to a set of
 * organizational business rules beyond schema validation. It examines:
 * </p>
 * 
 * <ul>
 * <li>Process naming conventions</li>
 * <li>Uniqueness of connection and transformation IDs</li>
 * <li>Correct references for connections and transformations</li>
 * <li>Uniqueness of endpoint paths</li>
 * <li>Presence of transformation descriptions</li>
 * <li>Completeness of data flow definitions</li>
 * </ul>
 * 
 * <p>
 * The validation produces {@link ValidationError} objects for rule
 * violations and {@link ValidationWarning} objects for recommendation
 * or potential improvement.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * BusinessRulesValidation validator = new BusinessRuleValidator();
 * SchemaValidationResult result = validator.validate(Paths.get("example.xml"));
 * 
 * if (result.isSchemaValid()) {
 *         System.out.println("Business rules passed!");
 * } else {
 *         result.getErrors().forEach(System.out::println);
 *         result.getWarnings().forEach(System.out::println);
 * }
 * }</pre>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-29
 */
@Slf4j
@Component
public class BusinessRulesValidation {

        /**
         * Regular expression that valid process names must match.
         * 
         * <p>
         * Must start with a letter and can contain letters, numbers, and underscores.
         * </p>
         */
        private static final String PROCESS_NAME_REGEX = "[A-Za-z][A-Za-z0-9_]*";

        /**
         * Validates the business rules of the given XML process file.
         * 
         * <p>
         * This method checks for rule violations and warnings, returning a
         * {@link SchemaValidationResult} that contains the results.
         * </p>
         * 
         * @param xmlFile the path to the XML file to validate
         * @return a {@code SchemaValidationResult} containing errors and warnings
         * @throws IllegalArgumentException if the file is {@code null} or cannot be
         *                                  read
         */
        public SchemaValidationResult validate(Path xmlFile) {
                log.info("Validation business rules for: {}", xmlFile.getFileName());

                List<ValidationError> errors = new ArrayList<>();
                List<ValidationWarning> warnings = new ArrayList<>();

                try {
                        Document doc = loadDocument(xmlFile);
                        XPath xpath = XPathFactory.newInstance().newXPath();

                        validateProcessName(doc, xpath, errors);
                        validateUniqueConnectionIds(doc, xpath, errors);
                        validateUniqueTransformationIds(doc, xpath, errors);
                        validateConnectionReferences(doc, xpath, errors);
                        validateTransformationReferences(doc, xpath, errors);
                        validateUniqueEndpointPaths(doc, xpath, warnings);
                        validateTransformationDescriptions(doc, xpath, warnings);
                        validateDataFlowCompleteness(doc, xpath, warnings);

                        SchemaValidationResult result = errors.isEmpty()
                                        ? SchemaValidationResult.valid("1.0")
                                        : SchemaValidationResult.invalid(errors);

                        result.setWarnings(warnings);
                        log.info("Business rule validation completed: {} errors, {} warnings",
                                        errors.size(), warnings.size());

                        return result;
                } catch (Exception e) {
                        log.error("Error during business rules validation", e);
                        errors.add(ValidationError.fatal("VALIDATION_ERROR",
                                        "Error during validation: " + e.getMessage()));

                        return SchemaValidationResult.invalid(errors);
                }
        }

        // Helper methods

        /**
         * Validates that the process name is present, sufficiently long, and
         * conforms to the naming convention.
         *
         * @param doc    the XML document to validate
         * @param xpath  the XPath evaluator
         * @param errors list to collect any validation errors
         * @throws Exception if an error occurs while evaluating XPath
         */
        private void validateProcessName(
                        Document doc,
                        XPath xpath,
                        List<ValidationError> errors) throws Exception {
                String processName = (String) xpath.evaluate(
                                "/process/@name",
                                doc,
                                XPathConstants.STRING);

                if (processName == null || processName.trim().isEmpty()) {
                        errors.add(ValidationError.error("PROCESS_NAME_MISSING",
                                        "Process name is required", 0, 0));
                } else if (processName.length() < 3) {
                        errors.add(ValidationError.warning("PROCESS_NAME_SHORT",
                                        "Process name should be at least 3 characters"));
                } else if (!processName.matches(PROCESS_NAME_REGEX)) {
                        errors.add(ValidationError.warning("PROCESS_NAME_INVALID",
                                        "Process name should start with a letter and contain only" +
                                                        " letters, numbers, and underscores"));
                }
        }

        /**
         * Checks for duplicate connection IDs in the XML document.
         *
         * @param doc    the XML document to validate
         * @param xpath  the XPath evaluator
         * @param errors list to collect any validation errors
         * @throws Exception if an error occurs while evaluating XPath
         */
        private void validateUniqueConnectionIds(
                        Document doc,
                        XPath xpath,
                        List<ValidationError> errors) throws Exception {
                NodeList connections = (NodeList) xpath.evaluate(
                                "//connections/connection/@id",
                                doc,
                                XPathConstants.NODESET);

                Set<String> ids = new HashSet<>();
                List<String> duplicates = new ArrayList<>();

                for (int i = 0; i < connections.getLength(); i++) {
                        String id = connections.item(i).getNodeValue();
                        if (!ids.add(id)) {
                                duplicates.add(id);
                        }
                }

                if (!duplicates.isEmpty()) {
                        errors.add(ValidationError.error("DUPLICATE_CONNECTION_IDS",
                                        "Duplicate connection IDs found: " +
                                                        String.join(", ", duplicates),
                                        0, 0));
                }
        }

        /**
         * Checks for duplicate transformation IDs in the XML document.
         *
         * @param doc    the XML document to validate
         * @param xpath  the XPath evaluator
         * @param errors list to collect any validation errors
         * @throws Exception if an error occurs while evaluating XPath
         */
        private void validateUniqueTransformationIds(
                        Document doc,
                        XPath xpath,
                        List<ValidationError> errors) throws Exception {
                NodeList transformations = (NodeList) xpath.evaluate(
                                "//transformations/transformation/@id",
                                doc,
                                XPathConstants.NODESET);

                Set<String> ids = new HashSet<>();
                List<String> duplicates = new ArrayList<>();

                for (int i = 0; i < transformations.getLength(); i++) {
                        String id = transformations.item(i).getNodeValue();
                        if (!ids.add(id)) {
                                duplicates.add(id);
                        }
                }

                if (!duplicates.isEmpty()) {
                        errors.add(ValidationError.error("DUPLICATE_TRANSFORMATION_IDS",
                                        "Duplicate transformation IDs found: " +
                                                        String.join(", ", duplicates),
                                        0, 0));
                }
        }

        /**
         * Validates that all source and target connection references in the data flow
         * exist in the set of defined connection IDs.
         *
         * @param doc    the XML document to validate
         * @param xpath  the XPath evaluator
         * @param errors list to collect any validation errors
         * @throws Exception if an error occurs while evaluating XPath
         */
        private void validateConnectionReferences(
                        Document doc,
                        XPath xpath,
                        List<ValidationError> errors) throws Exception {
                NodeList connectionIds = (NodeList) xpath.evaluate(
                                "//connections/connection/@id",
                                doc,
                                XPathConstants.NODESET);

                Set<String> definedIds = new HashSet<>();
                for (int i = 0; i < connectionIds.getLength(); i++) {
                        definedIds.add(connectionIds.item(i).getNodeValue());
                }

                String sourceConnRef = (String) xpath.evaluate(
                                "//dataFlow/source/connectionRef/text()",
                                doc,
                                XPathConstants.STRING);
                if (sourceConnRef != null && !sourceConnRef.isEmpty() &&
                                !definedIds.contains(sourceConnRef)) {
                        errors.add(ValidationError.error("INVALID_CONNECTION_REF",
                                        "Source connection reference '" + sourceConnRef +
                                                        "' does not exist",
                                        0, 0));
                }

                String targetConnRef = (String) xpath.evaluate(
                                "//dataFlow/target/connectionRef/text()",
                                doc,
                                XPathConstants.STRING);
                if (targetConnRef != null && !targetConnRef.isEmpty() &&
                                !definedIds.contains(targetConnRef)) {
                        errors.add(ValidationError.error("INVALID_CONNECTION_REF",
                                        "Target connection reference '" + targetConnRef +
                                                        "' does not exist",
                                        0, 0));
                }
        }

        /**
         * Validates that all transformation references in the data flow exist in
         * the set of defined transformation IDs.
         *
         * @param doc    the XML document to validate
         * @param xpath  the XPath evaluator
         * @param errors list to collect any validation errors
         * @throws Exception if an error occurs while evaluating XPath
         */
        private void validateTransformationReferences(
                        Document doc,
                        XPath xpath,
                        List<ValidationError> errors) throws Exception {
                NodeList transformationIds = (NodeList) xpath.evaluate(
                                "//transformations/transformation/@id",
                                doc,
                                XPathConstants.NODESET);

                Set<String> definedIds = new HashSet<>();
                for (int i = 0; i < transformationIds.getLength(); i++) {
                        definedIds.add(transformationIds.item(i).getNodeValue());
                }

                NodeList transRef = (NodeList) xpath.evaluate(
                                "//dataFlow/transformationRef/text()",
                                doc,
                                XPathConstants.NODESET);
                for (int i = 0; i < transRef.getLength(); i++) {
                        String ref = transRef.item(i).getNodeValue();

                        if (!definedIds.contains(ref)) {
                                errors.add(ValidationError.error("INVALID_TRANSFORMATION_REF",
                                                "Transformation reference '" + ref +
                                                                "' does not exist",
                                                0, 0));
                        }
                }
        }

        /**
         * Checks for duplicate endpoint paths and methods in the OpenAPI section.
         *
         * @param doc      the XML document to validate
         * @param xpath    the XPath evaluator
         * @param warnings list to collect any validation warnings
         * @throws Exception if an error occurs while evaluating XPath
         */
        private void validateUniqueEndpointPaths(
                        Document doc,
                        XPath xpath,
                        List<ValidationWarning> warnings) throws Exception {
                NodeList endpoints = (NodeList) xpath.evaluate(
                                "//openapi/endpoint",
                                doc,
                                XPathConstants.NODESET);

                Set<String> pathMethodPairs = new HashSet<>();
                List<String> duplicates = new ArrayList<>();

                for (int i = 0; i < endpoints.getLength(); i++) {
                        org.w3c.dom.Element endpoint = (org.w3c.dom.Element) endpoints.item(i);
                        String path = endpoint.getAttribute("path");
                        String method = endpoint.getAttribute("method");
                        String pair = method + " " + path;

                        if (!pathMethodPairs.add(pair)) {
                                duplicates.add(pair);
                        }
                }

                if (!duplicates.isEmpty()) {
                        warnings.add(ValidationWarning.builder()
                                        .code("DUPLICATE_ENDPOITNS")
                                        .message("Duplicate endpoint paths found: " +
                                                        String.join(", ", duplicates))
                                        .recommendation("Consider using unique paths for" +
                                                        " different operations")
                                        .build());
                }
        }

        /**
         * Ensures that all transformations have non-empty descriptions.
         *
         * @param doc      the XML document to validate
         * @param xpath    the XPath evaluator
         * @param warnings list to collect any validation warnings
         * @throws Exception if an error occurs while evaluating XPath
         */
        private void validateTransformationDescriptions(
                        Document doc,
                        XPath xpath,
                        List<ValidationWarning> warnings) throws Exception {
                NodeList transformations = (NodeList) xpath.evaluate(
                                "//transformations/transformation",
                                doc,
                                XPathConstants.NODESET);

                int missingDescriptions = 0;
                for (int i = 0; i < transformations.getLength(); i++) {
                        org.w3c.dom.Element transformation = (org.w3c.dom.Element) transformations.item(i);
                        NodeList descriptions = transformation.getElementsByTagName("description");

                        if (descriptions.getLength() == 0 ||
                                        descriptions.item(0).getTextContent().trim().isEmpty()) {
                                missingDescriptions++;
                        }
                }

                if (missingDescriptions > 0) {
                        warnings.add(ValidationWarning.builder()
                                        .code("MISSING_TRANSFORMATION_DESCRIPTIONS")
                                        .message(missingDescriptions + " transformation(s) are missing descriptions")
                                        .recommendation("Add descriptions to transformations for" +
                                                        " better documentation")
                                        .build());
                }
        }

        /**
         * Checks that the data flow section contains at least one source, one
         * transformation, and one target.
         *
         * @param doc      the XML document to validate
         * @param xpath    the XPath evaluator
         * @param warnings list to collect any validation warnings
         * @throws Exception if an error occurs while evaluating XPath
         */
        private void validateDataFlowCompleteness(
                        Document doc,
                        XPath xpath,
                        List<ValidationWarning> warnings) throws Exception {
                NodeList dataFlows = (NodeList) xpath.evaluate(
                                "//dataFlow",
                                doc,
                                XPathConstants.NODESET);

                if (dataFlows.getLength() == 0) {
                        warnings.add(ValidationWarning.builder()
                                        .code("MISSING_DATA_FLOW")
                                        .message("No data flow defined in the process")
                                        .recommendation("Define a data flow to document the complete" +
                                                        " data pipeline")
                                        .build());

                        return;
                }

                String source = (String) xpath.evaluate(
                                "//dataFlow/sources/entity/text()",
                                doc,
                                XPathConstants.STRING);
                if (source == null || source.trim().isEmpty()) {
                        warnings.add(ValidationWarning.builder()
                                        .code("INCOMPLETE_DATA_FLOW")
                                        .message("Data flow is missing source definitions")
                                        .recommendation("Define the source entity for the data flow")
                                        .build());
                }

                String target = (String) xpath.evaluate(
                                "//dataFlow/target/entity/text()",
                                doc,
                                XPathConstants.STRING);
                if (target == null || target.trim().isEmpty()) {
                        warnings.add(ValidationWarning.builder()
                                        .code("INCOMPLETE_DATA_FLOW")
                                        .message("Data flow is missing target definitions")
                                        .recommendation("Define the target entity for the data flow")
                                        .build());
                }
        }

        /**
         * Loads an XML document from the specified file path.
         *
         * @param xmlFile the XML file path
         * @return the parsed {@link Document}
         * @throws Exception if the file cannot be read or parsed
         */
        private Document loadDocument(Path xmlFile) throws Exception {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

                DocumentBuilder builder = factory.newDocumentBuilder();
                return builder.parse(xmlFile.toFile());
        }
}
