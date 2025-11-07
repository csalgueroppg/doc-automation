package com.ppg.iicsdoc.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;

public class SchemaValidatorTest {
    private SchemaValidator validator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        validator = new SchemaValidator();
    }

    @Test
    void shouldValidateValidXML() throws Exception {
        Path validXml = Paths.get("src/test/resources/sample-xml/cai-process.xml");
        SchemaValidationResult result = validator.validate(validXml);

        assertTrue(result.isValid());
        assertEquals(0, result.getErrorCount());
    }

    @Test 
    void shouldDetectMissingRequiredAttributes() throws Exception {
        String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <process type="CAI">
                    <metadata>
                        <description>Test</description>
                    </metadata>
                </process>
                """;

        Path xmlFile = tempDir.resolve("invalid.xml");
        Files.writeString(xmlFile, invalidXml);

        SchemaValidationResult result = validator.validate(xmlFile);
        assertFalse(result.isValid(), "Expected XML to be invalid due to missing required attributes");
        assertTrue(result.getErrorCount() > 0);
    }

    @Test 
    void shouldDetectInvalidProcessType() throws Exception {
        String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <process type="CAI" type="INVALID">
                    <metadata>
                        <description>Test</description>
                    </metadata>
                </process>
                """;

        Path xmlFile = tempDir.resolve("invalid-type.xml");
        Files.writeString(xmlFile, invalidXml);

        SchemaValidationResult result = validator.validate(xmlFile);
        assertFalse(result.isValid());
    }

    @Test
    void shouldHandleNonExistentFile() {
        Path nonExistent = tempDir.resolve("does-not-exist.xml");
        SchemaValidationResult result = validator.validate(nonExistent);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(e -> e.getCode().equals("FILE_NOT_FOUND")));
    }
}
