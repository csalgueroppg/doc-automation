package com.ppg.iicsdoc.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ppg.iicsdoc.fixtures.ParsedMetadataFixtures;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.domain.ProcessType;

class PromptBuilderTest {
    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder();
    }

    @Test
    void testBuildProcessFlowPrompt_withValidMetadata_shouldGeneratePrompt() {
        ParsedMetadata metadata = ParsedMetadataFixtures.withConnectionsAndTransformations();
        String prompt = promptBuilder.buildProcessFlowPrompt(metadata);

        assertNotNull(prompt);
        assertTrue(prompt.contains("flowchart"));
        assertTrue(prompt.contains("## Process Information"));
        assertTrue(prompt.contains("CustomerSync"));
        assertTrue(prompt.contains("CRM_DB"));
        assertTrue(prompt.contains("FilterActive"));
        assertTrue(prompt.contains("status = 'active'"));
        assertTrue(prompt.contains("MapFields"));
        assertTrue(prompt.contains("UPPER(name)"));
        assertTrue(prompt.contains("Return ONLY the Mermaid diagram code"));
    }

    @Test
    void testBuildProcessFlowPrompt_withMinimalMetadata_shouldStillGenerate() {
        ParsedMetadata metadata = ParsedMetadata.builder()
                .processName("minimal")
                .processType(ProcessType.CDI)
                .version("0.1")
                .build();

        String prompt = promptBuilder.buildProcessFlowPrompt(metadata);

        assertNotNull(prompt);
        assertTrue(prompt.contains("flowchart"));
        assertFalse(prompt.contains("## Connections"));
        assertFalse(prompt.contains("## Transformations"));
        assertFalse(prompt.contains("## Data Flo"));
    }

    @Test
    void testBuildAPIEndpoint_withValidEndpoints_shouldGeneratePrompt() {
        ParsedMetadata metadata = ParsedMetadataFixtures.withOpenAPIEndpoints();
        String prompt = promptBuilder.buildAPIEndpointPrompt(metadata);

        assertNotNull(prompt);
        assertTrue(prompt.contains("sequence diagram"));
        assertTrue(prompt.contains("GET /customers"));
        assertTrue(prompt.contains("Retrieve customer list"));
        assertTrue(prompt.contains("getCustomers"));
        assertTrue(prompt.contains("limit (integer)"));
        assertTrue(prompt.contains("offset (integer)"));
        assertTrue(prompt.contains("200"));
        assertTrue(prompt.contains("Return ONLY the Mermaid diagram code"));
    }

    @Test
    void testBuildAPIEndpointPrompt_withNoEndpoints_shouldReturnNull() {
        ParsedMetadata metadata = ParsedMetadataFixtures.empty();
        String prompt = promptBuilder.buildAPIEndpointPrompt(metadata);

        assertNull(prompt);
    }

    @Test
    void testBuildDataLineagePrompt_withValidationDataFlow_shouldGeneratePrompt() {
        ParsedMetadata metadata = ParsedMetadataFixtures.withProcessFlow();
        String prompt = promptBuilder.buildDataLineagePrompt(metadata);

        assertNotNull(prompt);
        assertTrue(prompt.contains("## Data Lineage"));
        assertTrue(prompt.contains("Source System"));
        assertTrue(prompt.contains("conn1"));
        assertTrue(prompt.contains("Source Entity"));
        assertTrue(prompt.contains("ERP_DB"));
        assertTrue(prompt.contains("Target System"));
        assertTrue(prompt.contains("conn2"));
        assertTrue(prompt.contains("Target Entity"));
        assertTrue(prompt.contains("CRM_API"));
        assertTrue(prompt.contains("Transformations Applied"));
        assertTrue(prompt.contains("trans1"));
        assertTrue(prompt.contains("Return ONLY the Mermaid diagram code"));
    }

    @Test
    void testBuildDataLineagePrompt_withNoDataFlow_shouldReturnNull() {
        ParsedMetadata metadata = ParsedMetadataFixtures.empty();
        String prompt = promptBuilder.buildDataLineagePrompt(metadata);

        assertNull(prompt);
    }

    @Test
    void testPromptInstructions_shouldBePresentInAllPrompts() {
        ParsedMetadata metadata = ParsedMetadataFixtures.withConnectionsAndTransformations();

        String flowPrompt = promptBuilder.buildProcessFlowPrompt(metadata);
        String lineagePrompt = promptBuilder.buildDataLineagePrompt(metadata);
        String apiPrompt = promptBuilder.buildAPIEndpointPrompt(ParsedMetadataFixtures.withOpenAPIEndpoints());

        assertTrue(flowPrompt.contains("Return ONLY the Mermaid diagram code"));
        assertTrue(lineagePrompt.contains("Return ONLY the Mermaid diagram code"));
        assertTrue(apiPrompt.contains("Return ONLY the Mermaid diagram code"));
    }

    @Test
    void testBuildProcessFlowPrompt_shouldIncludeAllSectionHeaders() {
        ParsedMetadata metadata = ParsedMetadataFixtures.withConnectionsAndTransformations();
        String prompt = promptBuilder.buildProcessFlowPrompt(metadata);

        assertTrue(prompt.contains("## Process Information"));
        assertTrue(prompt.contains("## Connections"));
        assertTrue(prompt.contains("## Transformations"));
        assertTrue(prompt.contains("## Data Flow"));
        assertTrue(prompt.contains("## Instructions"));
    }
}
