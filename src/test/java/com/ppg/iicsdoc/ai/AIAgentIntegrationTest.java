package com.ppg.iicsdoc.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.ppg.iicsdoc.model.ai.MermaidDiagram;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.parser.XMLParserService;

/**
 * Integration tests for the {@link AIAgentService} using real AI API calls.
 * 
 * These tests are disabled by default to avoid incurring costs or hitting rate limits.
 * They can be enabled when a valid AI API key is provided in the test environment.
 * 
 * <p>
 * Note: Ensure that the AI API key is set in the environment variables or application 
 * properties before running these tests.
 * </p>
 * 
 * <p>
 * To run these tests, remove the {@code @Disabled} annotation and provide a valid AI API key.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires valid AI API key")
class AIAgentIntegrationTest {

    @Autowired
    private AIAgentService aiAgentService;

    @Autowired
    private XMLParserService xmlParserService;

    @Test
    void shouldGenerateDiagramFromRealXML() throws Exception {
        Path xmlFile = Paths.get("/src/test/resources/sample-xml/simple-cai-process.xml");
        ParsedMetadata metadata = xmlParserService.parse(xmlFile);
        MermaidDiagram diagram = aiAgentService.generateProcessFlowDiagram(metadata);

        assertNotNull(diagram);
        assertTrue(diagram.hasContent());
        assertTrue(diagram.isValidated());

        System.out.println("Generated Diagram");
        System.out.println(diagram.toMarkdown());
    }

    @Test
    void shouldGenerateAPIEndpointDiagram() throws Exception {
        Path xmlFile = Paths.get("/src/test/resources/sample-xml/api-endpoint-process.xml");
        ParsedMetadata metadata = xmlParserService.parse(xmlFile);
        MermaidDiagram diagram = aiAgentService.generateApiEndpointDiagram(metadata);

        assertNotNull(diagram);
        assertTrue(diagram.hasContent());
        assertTrue(diagram.isValidated());

        System.out.println("Generated API Endpoint Diagram");
        System.out.println(diagram.toMarkdown());
    }
}
