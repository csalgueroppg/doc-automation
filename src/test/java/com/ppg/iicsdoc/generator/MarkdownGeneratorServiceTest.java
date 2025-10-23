package com.ppg.iicsdoc.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ppg.iicsdoc.exception.ValidationException;
import com.ppg.iicsdoc.fixtures.ParsedMetadataFixtures;
import com.ppg.iicsdoc.model.ai.MermaidDiagram;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.domain.ProcessType;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;

@SpringBootTest
public class MarkdownGeneratorServiceTest {

    @Autowired
    private MarkdownGeneratorService markdownGenerator;

    private ParsedMetadata sampleMetadata;
    private MermaidDiagram sampleDiagram;

    @BeforeEach
    void setUp() {
        sampleMetadata = ParsedMetadataFixtures.createSimpleMetadata();
        sampleDiagram = createSampleDiagram();
    }

    @Test
    void shouldGenerateMarkdownDocument() {
        MarkdownDocument document = markdownGenerator.generate(sampleMetadata, sampleDiagram);

        assertNotNull(document);
        assertTrue(document.hasContent());
        assertNotNull(document.getFilename());
        assertTrue(document.getFilename().endsWith(".md"));
        assertEquals("CustomerDataSync", document.getTitle());
        assertNotNull(document.getGeneratedAt());
    }

    @Test
    void shouldIncludeProcessName() {
        MarkdownDocument document = markdownGenerator.generate(sampleMetadata, sampleDiagram);
        assertTrue(document.getContent().contains("CustomerDataSync"));
    }

    @Test 
    void shouldIncludeProcessType() {
        MarkdownDocument document = markdownGenerator.generate(sampleMetadata, sampleDiagram);
        assertTrue(document.getContent().contains("Cloud Application Integration"));
    }

    @Test 
    void shouldIncludeDiagram() {
        MarkdownDocument document = markdownGenerator.generate(sampleMetadata, sampleDiagram);
        
        assertTrue(document.getContent().contains("```mermaid"));
        assertTrue(document.getContent().contains("flowchart TD"));
    }

    @Test 
    void shouldIncludeConnections() {
        MarkdownDocument document = markdownGenerator.generate(sampleMetadata, sampleDiagram);

        assertTrue(document.getContent().contains("## Connections"));
        assertTrue(document.getContent().contains("CustomerAPI"));
        assertTrue(document.getContent().contains("REST"));
    }

    @Test 
    void shouldIncludeTransformations() {
        MarkdownDocument document = markdownGenerator.generate(sampleMetadata, sampleDiagram);

        assertTrue(document.getContent().contains("## Transformations"));
        assertTrue(document.getContent().contains("FormatCustomerData"));
        assertTrue(document.getContent().contains("EXPRESSION"));
    }

    @Test 
    void shouldIncludeDataflow() {
        MarkdownDocument document = markdownGenerator.generate(sampleMetadata, sampleDiagram);

        assertTrue(document.getContent().contains("## Data Flow"));
        assertTrue(document.getContent().contains("conn1"));
        assertTrue(document.getContent().contains("Customer"));
    }

    @Test 
    void shouldIncludeAPIEndpoints() {
        MarkdownDocument document = markdownGenerator.generate(sampleMetadata, sampleDiagram);

        assertTrue(document.getContent().contains("## API Endpoints"));
        assertTrue(document.getContent().contains("/customers"));
        assertTrue(document.getContent().contains("GET"));
    }

    @Test 
    void shouldIncludeBothDiagrams() {
        MermaidDiagram apiDiagram = MermaidDiagram.builder()
            .diagramCode("sequenceDiagram\n   Client->>Server: Request")
            .type(MermaidDiagram.DiagramType.SEQUENCE)
            .build();

        MarkdownDocument document = markdownGenerator.generate(
            sampleMetadata,
            sampleDiagram,
            apiDiagram
        );

        assertTrue(document.getContent().contains("## Process Flow Diagram"));
        assertTrue(document.getContent().contains("## API Sequence Diagram"));
    }

    @Test 
    void shouldGenerateValidFileName() {
        MarkdownDocument document = markdownGenerator.generate(sampleMetadata, sampleDiagram);

        assertEquals("customerdatasync.md", document.getFilename());
    }

    @Test 
    void shouldThrowExceptionForNullMetadata() {
        assertThrows(NullPointerException.class, () -> {
            markdownGenerator.generate(null, sampleDiagram);
        });
    }

    @Test 
    void shouldThrowExceptionForNullDiagram() {
        assertThrows(ValidationException.class, () -> {
            markdownGenerator.generate(sampleMetadata, null);
        });
    }

    @Test 
    void shouldThrowExceptionForMetadataWithoutProcessName() {
        ParsedMetadata invalidMetadata = ParsedMetadata.builder()
            .processType(ProcessType.CAI)
            .build();

        assertThrows(ValidationException.class, () -> {
           markdownGenerator.generate(invalidMetadata, sampleDiagram); 
        });
    }

    @Test 
    void shouldIncludeTableOfContents() {
        MarkdownDocument document = markdownGenerator.generate(sampleMetadata, sampleDiagram);
        assertTrue(document.getContent().contains("## Table of Contents"));
    }

    @Test 
    void shouldIncludeFrontMatter() {
        MarkdownDocument document = markdownGenerator.generate(sampleMetadata, sampleDiagram);

        assertTrue(document.getContent().startsWith("---"));
        assertTrue(document.getContent().contains("title: CustomerDataSync"));
    }

    private MermaidDiagram createSampleDiagram() {
        String diagramCode = """
                flowchart TD
                    Start[Start Process] --> Fetch[Fetch Customer Data]
                    Fetch --> Transform[Format Customer Data]
                    Transform --> Load[Load to Database]
                    Load --> End[End Process]
                """;

        return MermaidDiagram.builder()
                .diagramCode(diagramCode)
                .type(MermaidDiagram.DiagramType.FLOWCHART)
                .validated(true)
                .build();
    }
}
