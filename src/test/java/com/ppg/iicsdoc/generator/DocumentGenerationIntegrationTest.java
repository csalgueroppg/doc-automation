package com.ppg.iicsdoc.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.ppg.iicsdoc.model.ai.MermaidDiagram;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;
import com.ppg.iicsdoc.parser.XMLParserService;
import com.ppg.iicsdoc.validation.BusinessRulesValidation;
import com.ppg.iicsdoc.validation.SchemaValidator;
import com.ppg.iicsdoc.validation.WellFormednessValidator;
import com.ppg.iicsdoc.validation.XMLValidationService;
import com.ppg.iicsdoc.validation.cache.ValidationCacheService;

import lombok.extern.slf4j.Slf4j;

/**
 * Integration test for complete documentation generation 
 * pipeline.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class DocumentGenerationIntegrationTest {
    private XMLParserService parserService;

    @Autowired
    private MarkdownGeneratorService markdownGenerator;

    @BeforeEach
    void setUp() {
        WellFormednessValidator wellFormednessValidator = new WellFormednessValidator();
        SchemaValidator schemaValidator = new SchemaValidator();
        BusinessRulesValidation businessRulesValidator = new BusinessRulesValidation();
        ValidationCacheService cacheService = new ValidationCacheService();

        XMLValidationService validationService = new XMLValidationService(
                schemaValidator,
                businessRulesValidator,
                wellFormednessValidator, 
                cacheService);

        parserService = new XMLParserService(validationService);
    }

    @Test
    void shouldGenerateCompleteDocumentation() throws Exception {
        Path xmlFile = Path.of("src/test/resources/sample-xml/cai-process.xml");

        log.info("Step 1: Parsing XML file");
        ParsedMetadata metadata = parserService.parse(xmlFile);

        assertNotNull(metadata);
        assertEquals("SDasCustomerSync", metadata.getProcessName());

        log.info("Step 2: Create mock diagram (in real scenario, this would add AI integration)");
        MermaidDiagram diagram = createMockDiagram();

        assertNotNull(diagram);
        assertTrue(diagram.hasContent());

        log.info("Step 3: Generating markdown documentation");
        MarkdownDocument document = markdownGenerator.generate(metadata, diagram);

        assertNotNull(document);
        assertTrue(document.hasContent());

        String content = document.getContent();
        assertTrue(content.contains("CustomerDataSync"));
        assertTrue(content.contains("## Connections"));
        assertTrue(content.contains("## Transformations"));
        assertTrue(content.contains("## Data Flow"));
        assertTrue(content.contains("```mermaid"));

        log.info("Step 4: Write to file");
        Path outputPath = Path.of("target", document.getFilename());
        Files.writeString(outputPath, content);

        log.info("Document written to: {}", outputPath.toAbsolutePath());
        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 0);

        log.info("Integration test completed successfully");
    }

    private MermaidDiagram createMockDiagram() {
        String diagramCode = """
                flowchart TD
                    Start[Start: CustomerDataSync] --> Connect[Connect to CustomerAPI]
                    Connect --> Fetch[Fetch Customer Data]
                    Fetch --> Trans1[Transform: FormatCustomerData]
                    Trans1 --> Trans2[Filter: ActiveCustomersOnly]
                    Trans2 --> Load[Load to CustomerDB]
                    Load --> End[End Process]

                    style Start fill:#90EE90
                    style End fill:#FFB6C1
                    style Trans1 fill:#87CEEB
                    style Trans2 fill:#87CEEB
                """;
            
        return MermaidDiagram.builder()
            .diagramCode(diagramCode)
            .type(MermaidDiagram.DiagramType.FLOWCHART)
            .title("SDasCustomerSync - Process Flow")
            .validated(true)
            .validationMessage("Valid")
            .build();
    }
}
