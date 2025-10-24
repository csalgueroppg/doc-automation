package com.ppg.iicsdoc.deployer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ppg.iicsdoc.config.AppConfig;
import com.ppg.iicsdoc.model.deployment.DeploymentConfig;
import com.ppg.iicsdoc.model.deployment.DeploymentResult;
import com.ppg.iicsdoc.model.deployment.DeploymentStrategy;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;

@ExtendWith(MockitoExtension.class)
class DeploymentServiceTest {
 
    @Mock
    private LocalFileSystemHandler fileSystemHandler;

    @Mock 
    private DocusaurusHandler docusaurusHandler;

    @Mock 
    private GitDeploymentHandler gitHandler;

    private DeploymentService deploymentService;
    private AppConfig.DeploymentProperties deploymentProperties;

    @BeforeEach
    void setUp() {
        deploymentProperties = new AppConfig.DeploymentProperties();
        deploymentProperties.setDocusuarusPath("./docs");

        deploymentService = new DeploymentService(
            fileSystemHandler,
            docusaurusHandler,
            gitHandler,
            deploymentProperties
        );
    }

    @Test
    void shouldDeployWithDefaultConfig() {
        MarkdownDocument document = createSampleDocument();
        DeploymentConfig config = DeploymentConfig.docusaurus("./docs");
        DeploymentResult mockResult = DeploymentResult.success(
            "./docs",
            List.of("test.md"),
            DeploymentStrategy.DOCUSAURUS);

        when(docusaurusHandler.deploy(document, config)).thenReturn(mockResult);

        DeploymentResult result = deploymentService.deploy(document, config);
        assertTrue(result.isSuccess());
        verify(docusaurusHandler, times(1)).deploy(document, config);
    }

    @Test 
    void shouldDeployWithCustomConfig() {
        MarkdownDocument document = createSampleDocument();
        DeploymentConfig config = DeploymentConfig.localFilesystem("./output");
        DeploymentResult mockResult = DeploymentResult.success(
            "./output",
            List.of("test.md"),
            DeploymentStrategy.LOCAL_FILESYSTEM);

        when(fileSystemHandler.deploy(document, config)).thenReturn(mockResult);

        DeploymentResult result = deploymentService.deploy(document, config);
        assertTrue(result.isSuccess());
        verify(fileSystemHandler, times(1)).deploy(document, config);
    }

    @Test
    void shouldDeployBatch() {
        List<MarkdownDocument> documents = List.of(
            createSampleDocument(),
            createSampleDocument()
        );

        DeploymentResult mockResult = DeploymentResult.success(
            "./docs",
            List.of("test1.md", "test2.md"),
            DeploymentStrategy.DOCUSAURUS);

        when(docusaurusHandler.deployBatch(eq(documents), any())).thenReturn(mockResult);

        DeploymentResult result = deploymentService.deployBatch(documents);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getDeployedFileCount());
        verify(docusaurusHandler, times(1)).deployBatch(eq(documents), any());
    }

    @Test 
    void shouldRollbackDeployment() {
        DeploymentResult deploymentResult = DeploymentResult.success(
            "./docs",
            List.of("test.md"),
            DeploymentStrategy.DOCUSAURUS);

        when(docusaurusHandler.rollback(deploymentResult)).thenReturn(true);

        boolean result = deploymentService.rollback(deploymentResult);
        assertTrue(result);
        verify(docusaurusHandler, times(1)).rollback(deploymentResult);
    }

    @Test 
    void shouldValidateConfig() {
        DeploymentConfig config = DeploymentConfig.localFilesystem("./output");
        when(fileSystemHandler.validateConfig(config)).thenReturn(true);

        boolean result = deploymentService.validateConfig(config);
        assertTrue(result);
        verify(fileSystemHandler, times(1)).validateConfig(config);
    }

    private MarkdownDocument createSampleDocument() {
        return MarkdownDocument.builder()
            .filename("test.md")
            .title("Test")
            .content("# Test\n\nContent")
            .generatedAt(LocalDateTime.now())
            .build();
    }
}
