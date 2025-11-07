package com.ppg.iicsdoc.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.parser.XMLParserService;
import com.ppg.iicsdoc.validation.XMLValidationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class CompleteValidationIntegrationTest {

    @Autowired
    private XMLValidationService validationService;

    @Autowired
    private XMLParserService parserService;

    @Test
    void shouldValidateSimpleCAIProcess() {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/cai-process.xml");
        SchemaValidationResult result = validationService.validateComplete(xmlFile);

        assertTrue(result.isValid(), "Simple CAI process should be valid");
        assertFalse(result.getErrorCount() > 0, "There should be no errors in this file");
    }

    @Test
    void shouldValidateComplexCDIProcess() {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/complex-cdi-process.xml");
        SchemaValidationResult result = validationService.validateComplete(xmlFile);

        assertTrue(result.isValid(), "Complex CDI process should be valid");
        assertNotNull(result.getMetrics());
        assertTrue(result.getMetrics().getValidationDurationMs() > 0);
    }

    @Test
    void shouldValidateAPIGatewayProcess() {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/api-gateway-cai-process.xml");
        SchemaValidationResult result = validationService.validateComplete(xmlFile);

        assertTrue(result.isValid());
        assertFalse(result.getErrorCount() > 0, "There should be no errors on this file");
    }

    @Test
    void shouldRejectInvalidMissingRequired() {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/invalid-missing-required.xml");
        SchemaValidationResult result = validationService.validateComplete(xmlFile);

        assertFalse(result.isValid());
        assertTrue(result.getErrorCount() > 0);
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getMessage().contains("name")));
    }

    @Test
    void shouldDetectDuplicateIds() {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/invalid-duplicate-ids.xml");
        SchemaValidationResult result = validationService.validateComplete(xmlFile);

        assertFalse(result.isValid());
        assertTrue(result.getErrorCount() > 0);
    }

    @Test
    void shouldDetectBrokenReference() {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/invalid-broken-reference.xml");
        SchemaValidationResult result = validationService.validateComplete(xmlFile);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getCode().equals("INVALID_CONNECTION_REF")));
    }

    @Test
    void shouldParseAfterValidation() {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/complex-cdi-process.xml");
        SchemaValidationResult validationResult = validationService.validateComplete(xmlFile);
        assertTrue(validationResult.isValid());

        var metadata = parserService.parse(xmlFile);
        assertNotNull(metadata);
        assertEquals("EnterpriseDataPipeline", metadata.getProcessName());
        assertEquals("CDI", metadata.getProcessType().name());
        assertTrue(metadata.getConnections().size() >= 5);
        assertTrue(metadata.getTransformations().size() >= 7);
    }

    @Test
    void shouldHandleLargeFiles() {
        Path xmlFile = Paths.get("src/test/resources/sample-xml-generated/large-process.xml");

        long startTime = System.currentTimeMillis();
        SchemaValidationResult result = validationService.validateComplete(xmlFile);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(result.isValid());
        assertTrue(duration < 5000, "Validation should complete under 5 seconds");
    }

    @Test
    void shouldRejectAllInvalidFiles() {
        Path[] invalidFiles = {
                Paths.get("src/test/resources/sample-xml/invalid-missing-required.xml"),
                Paths.get("src/test/resources/sample-xml/invalid-duplicate-ids.xml"),
                Paths.get("src/test/resources/sample-xml/invalid-broken-reference.xml")
        };

        for (Path file : invalidFiles) {
            SchemaValidationResult result = validationService.validateComplete(file);
            assertFalse(result.isValid(), "File should be invalid: " + file.getFileName());

            log.info("Correctly rejected: {} with {} errors",
                    file.getFileName(), result.getErrorCount());
        }
    }
}
