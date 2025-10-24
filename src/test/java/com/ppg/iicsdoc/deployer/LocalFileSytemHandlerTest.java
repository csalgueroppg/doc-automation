package com.ppg.iicsdoc.deployer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ppg.iicsdoc.model.deployment.DeploymentConfig;
import com.ppg.iicsdoc.model.deployment.DeploymentResult;
import com.ppg.iicsdoc.model.deployment.DeploymentStrategy;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;

class LocalFileSytemHandlerTest {

    private LocalFileSystemHandler handler;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        handler = new LocalFileSystemHandler();
    }

    @Test
    void shouldDeployDocumentToFilesystem() throws Exception {
        MarkdownDocument document = createSampleDocument();
        DeploymentConfig config = DeploymentConfig.localFilesystem(tempDir.toString());
        DeploymentResult result = handler.deploy(document, config);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getDeployedFileCount());
        assertTrue(Files.exists(tempDir.resolve("test-document.md")));
    }

    @Test
    void shouldCreateBackupWhenFileExists() throws Exception {
        MarkdownDocument document = createSampleDocument();
        Path targetFile = tempDir.resolve("test-document.md");
        Files.writeString(targetFile, "Old content");

        DeploymentConfig config = DeploymentConfig.localFilesystem(tempDir.toString());
        config.setCreateBackup(true);

        DeploymentResult result = handler.deploy(document, config);
        assertTrue(result.isSuccess());

        List<Path> backups = Files.list(tempDir)
                .filter(p -> p.getFileName().toString().contains("backup_"))
                .toList();

        assertFalse(backups.isEmpty());
    }

    @Test
    void shouldDeployBatchDocuments() throws Exception {
        List<MarkdownDocument> documents = List.of(
                createSampleDocument("doc1.md", "Document 1"),
                createSampleDocument("doc2.md", "Document 2"),
                createSampleDocument("doc3.md", "Document 3"));

        DeploymentConfig config = DeploymentConfig.localFilesystem(tempDir.toString());
        DeploymentResult result = handler.deployBatch(documents, config);

        assertTrue(result.isSuccess());
        assertEquals(3, result.getDeployedFileCount());
        assertTrue(Files.exists(tempDir.resolve("doc1.md")));
        assertTrue(Files.exists(tempDir.resolve("doc2.md")));
        assertTrue(Files.exists(tempDir.resolve("doc3.md")));
    }

    @Test
    void shouldValidateDeployment() throws Exception {
        MarkdownDocument document = createSampleDocument();
        DeploymentConfig config = DeploymentConfig.localFilesystem(tempDir.toString());
        config.setValidateAfterDeployment(true);

        DeploymentResult result = handler.deploy(document, config);
        assertTrue(result.isSuccess());

        String deployedContent = Files.readString(tempDir.resolve("test-document.md"));
        assertEquals(document.getContent(), deployedContent);
    }

    @Test
    void shouldRollbackDeployment() throws Exception {
        MarkdownDocument document = createSampleDocument();
        Path targetFile = tempDir.resolve(document.getFilename());

        Files.writeString(targetFile, "Original content");

        DeploymentConfig config = DeploymentConfig.localFilesystem(tempDir.toString());
        config.setCreateBackup(true);

        DeploymentResult result = handler.deploy(document, config);
        assertTrue(result.isSuccess());

        DeploymentResult fixedResult = DeploymentResult.builder()
                .success(true)
                .targetPath(tempDir.toString())
                .deployedFiles(List.of(document.getFilename()))
                .deployedAt(result.getDeployedAt())
                .message(result.getMessage())
                .strategy(result.getStrategy())
                .build();

        boolean rollbackSuccess = handler.rollback(fixedResult);
        assertTrue(rollbackSuccess);

        String restoredContent = Files.readString(targetFile);
        assertEquals("Original content", restoredContent);
    }

    @Test
    void shouldFailValidationForInvalidConfig() {
        DeploymentConfig invalidConfig = DeploymentConfig.builder()
                .strategy(DeploymentStrategy.LOCAL_FILESYSTEM)
                .build();

        boolean isValid = handler.validateConfig(invalidConfig);
        assertFalse(isValid);
    }

    @Test
    void shouldCreateDirectoryIfNotExists() throws Exception {
        Path newDir = tempDir.resolve("subdir/nested");
        MarkdownDocument document = createSampleDocument();
        DeploymentConfig config = DeploymentConfig.localFilesystem(newDir.toString());
        DeploymentResult result = handler.deploy(document, config);

        assertTrue(result.isSuccess());
        assertTrue(Files.isDirectory(newDir));
        assertTrue(Files.exists(newDir.resolve("test-document.md")));
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
