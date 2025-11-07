package com.ppg.iicsdoc.pipeline;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.ppg.iicsdoc.ai.AIAgentService;
import com.ppg.iicsdoc.deployer.DeploymentService;
import com.ppg.iicsdoc.generator.MarkdownGeneratorService;
import com.ppg.iicsdoc.model.ai.MermaidDiagram;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;
import com.ppg.iicsdoc.parser.XMLParserService;
import com.ppg.iicsdoc.validation.XMLValidationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AsyncDocumentationPipeline {
    
    private final XMLValidationService validationService;
    private final XMLParserService xmlParser;
    private final AIAgentService aiAgent;
    private final MarkdownGeneratorService markdownGenerator;
    private final DeploymentService deploymentService;
    private final ExecutorService executorService;

    public AsyncDocumentationPipeline(
        XMLValidationService validationService,
        XMLParserService xmlParser,
        AIAgentService aiAgent,
        MarkdownGeneratorService markdownGenerator,
        DeploymentService deploymentService) {
        this.validationService = validationService;
        this.xmlParser = xmlParser;
        this.aiAgent = aiAgent;
        this.markdownGenerator = markdownGenerator;
        this.deploymentService = deploymentService;
        this.executorService = Executors.newWorkStealingPool();
    }

    public CompletableFuture<MarkdownDocument> processAsync(Path xmlFile) {
        return CompletableFuture 
            .supplyAsync(() -> {
              log.info("Validating XML file");
              var result = validationService.validateComplete(xmlFile);
              
              if (!result.isValid()) {
                throw new RuntimeException("Validation failed");
              }

              return xmlFile;
            }, executorService)
            .thenApplyAsync(file -> {
                log.info("Parsing XML file");
                try {
                    return xmlParser.parse(file);
                } catch (Exception e) {
                    throw new RuntimeException("Parsing failed", e);
                }
            }, executorService)
            .thenComposeAsync(metadata -> {
                log.info("Generating diagrams");
                CompletableFuture<MermaidDiagram> flowDiagramFuture = 
                    CompletableFuture.supplyAsync(() -> 
                        aiAgent.generateProcessFlowDiagram(metadata), executorService);
                
                CompletableFuture<MermaidDiagram> apiDiagramFuture = 
                    CompletableFuture.supplyAsync(() -> 
                        aiAgent.generateApiEndpointDiagram(metadata), executorService);

                return flowDiagramFuture.thenCombine(apiDiagramFuture, 
                    (flowDiagram, apiDiagram) -> new DiagramPair(metadata, flowDiagram, apiDiagram));
            }, executorService)
            .thenApplyAsync(pair -> {
                log.info("Generating markdown file");
                return markdownGenerator.generate(
                    pair.metadata,
                    pair.flowDiagram,
                    pair.apiDiagram);
            }, executorService)
            .thenApplyAsync(document -> {
                log.info("Deploying document to the specified path");
                try {
                    deploymentService.deploy(document);
                    return document;
                } catch (Exception e) {
                    throw new RuntimeException("Deployment failed", e);
                }
            }, executorService)
            .exceptionally(ex -> {
                log.error("Async pipeline failed", ex);
                throw new RuntimeException("Async Pipeline execution failed", ex);
            });       
    }

    private record DiagramPair(
        ParsedMetadata metadata,
        MermaidDiagram flowDiagram,
        MermaidDiagram apiDiagram
    ){}
    
}
