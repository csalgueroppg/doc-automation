package com.ppg.iicsdoc.deployer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ppg.iicsdoc.model.deployment.DeploymentConfig;
import com.ppg.iicsdoc.model.deployment.DeploymentResult;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;

class DocusaurusHandlerTest {

    private DocusaurusHandler handler;
    private LocalFileSystemHandler fileSystemHandler;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileSystemHandler = new LocalFileSystemHandler();
        handler = new DocusaurusHandler(fileSystemHandler);
    }

    @Test
    void shouldDeployToDocusaurus() throws Exception {
        MarkdownDocument document = createSampleDocument();
        DeploymentConfig config = DeploymentConfig.docusaurus(tempDir.toString());
        config.setDocusaurusCategory("getting-started");

        DeploymentResult result = handler.deploy(document, config);

        assertTrue(result.isSuccess());
        assertTrue(Files.exists(tempDir.resolve("getting-started").resolve("test-document.md")));
    }

    @Test
    void shouldAddDocusaurusFrontMatter() throws Exception {
        MarkdownDocument document = createSampleDocument();
        DeploymentConfig config = DeploymentConfig.docusaurus(tempDir.toString());
        config.setDocusaurusCategory("getting-started");

        DeploymentResult result = handler.deploy(document, config);
        assertTrue(result.isSuccess());

        String content = Files.readString(tempDir
                .resolve("getting-started")
                .resolve("test-document.md"));
        assertTrue(content.contains("id:"));
        assertTrue(content.contains("title:"));
        assertTrue(content.contains("sidebar_label:"));
    }

    @Test
    void shouldCreateCategoryMetadata() throws Exception {
        MarkdownDocument document = createSampleDocument();
        DeploymentConfig config = DeploymentConfig.docusaurus(tempDir.toString());
        config.setDocusaurusCategory("iics-processes");

        DeploymentResult result = handler.deploy(document, config);
        assertTrue(result.isSuccess());

        Path categoryPath = tempDir.resolve("iics-processes");
        assertTrue(Files.isDirectory(categoryPath));
        assertTrue(Files.exists(categoryPath.resolve("_category.json")));

        String categoryContent = Files.readString(categoryPath.resolve("_category.json"));
        assertTrue(categoryContent.contains("\"label\""));
    }

    @Test
    void shouldDeployToCategory() throws Exception {
        MarkdownDocument document = createSampleDocument();
        DeploymentConfig config = DeploymentConfig.docusaurus(tempDir.toString());
        config.setDocusaurusCategory("my-category");

        DeploymentResult result = handler.deploy(document, config);
        assertTrue(result.isSuccess());
        assertTrue(Files.exists(tempDir.resolve("my-category/test-document.md")));
    }

    @Test
    void shouldValidateDocusaurusConfig() {
        DeploymentConfig validConfig = DeploymentConfig.docusaurus(tempDir.toString());
        DeploymentConfig invalidConfig = DeploymentConfig.builder().build();

        assertTrue(handler.validateConfig(validConfig));
        assertFalse(handler.validateConfig(invalidConfig));
    }

    private MarkdownDocument createSampleDocument() {
        return createSampleDocument("test-document.md", "Test Document");
    }

    private MarkdownDocument createSampleDocument(String fileName, String title) {
        return MarkdownDocument.builder()
                .filename(fileName)
                .title(title)
                .content("# " + title + "\n\nThis is test content.")
                .generatedAt(LocalDateTime.now())
                .build();
    }
}
