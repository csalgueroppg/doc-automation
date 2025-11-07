package com.ppg.iicsdoc.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.ppg.iicsdoc.deployer.DeploymentService;
import com.ppg.iicsdoc.generator.MarkdownGeneratorService;
import com.ppg.iicsdoc.model.ai.MermaidDiagram;
import com.ppg.iicsdoc.model.deployment.DeploymentConfig;
import com.ppg.iicsdoc.model.deployment.DeploymentResult;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;
import com.ppg.iicsdoc.parser.XMLParserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class CompleteWorkflowIntegrationTest {

    @Autowired
    private XMLParserService xmlParser;

    @Autowired
    private MarkdownGeneratorService markdownGenerator;

    @Autowired
    private DeploymentService deploymentService;

    @TempDir
    Path tempDeploymentDir;

    @Test
    void shouldCompleteFullWorkflow() throws Exception {
        log.info("Starting Complete Workflow Integration Test");

        log.info("Step 1: Parsing XML file");
        Path xmlFile = Paths.get("src/test/resources/sample-xml/cai-process.xml");
        ParsedMetadata metadata = xmlParser.parse(xmlFile);

        assertNotNull(metadata);
        log.info("XML parsed successfully: {}", metadata.getProcessName());

        log.info("Step 2: Generating Diagram");
        MermaidDiagram diagram = createMockDiagram();

        assertNotNull(diagram);
        assertTrue(diagram.hasContent());
        log.info("Diagram generated successfully");

        log.info("Step 3: Generating markdown");
        MarkdownDocument document = markdownGenerator.generate(metadata, diagram);
        
        assertNotNull(document);
        assertTrue(document.hasContent());
        log.info("Markdown generated successfully: {} ({} bytes)", 
            document.getFilename(), 
            document.getSize());

        log.info("Step 4: Deploying documents");
        DeploymentConfig config = DeploymentConfig.localFilesystem(tempDeploymentDir.toString());
        DeploymentResult result = deploymentService.deploy(document, config);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getDeployedFileCount());
        log.info("Documentation deployed successfully to: {}", result.getTargetPath());

        log.info("Step 5: Verifying deployment");
        Path deployedFile = tempDeploymentDir.resolve(document.getFilename());
        assertTrue(Files.exists(deployedFile));

        String deployedContent = Files.readString(deployedFile);
        assertEquals(document.getContent(), deployedContent);

        log.info("Deploymend verified successfully");
        log.info("Complete workflow integratoin test PASSED");
    }

    private MermaidDiagram createMockDiagram() {
        String code = """
                flowchart TD
                    Start[Start Process] --> Fetch[Fetch Data]
                    Fetch --> Transform[Transform Data]
                    Transform --> Load[Load Data]
                    Load --> End[End Process]
                """;

        return MermaidDiagram.builder()
                .diagramCode(code)
                .type(MermaidDiagram.DiagramType.FLOWCHART)
                .validated(true)
                .build();
    }
}
