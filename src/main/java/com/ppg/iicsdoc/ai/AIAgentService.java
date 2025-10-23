package com.ppg.iicsdoc.ai;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.ppg.iicsdoc.config.AppConfig;
import com.ppg.iicsdoc.exception.AIServiceException;
import com.ppg.iicsdoc.model.ai.AIRequest;
import com.ppg.iicsdoc.model.ai.AIResponse;
import com.ppg.iicsdoc.model.ai.MermaidDiagram;
import com.ppg.iicsdoc.model.common.ValidationResult;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for interacting with the AI agent to generate diagrams based on the 
 * provided parsed metadata ({@link ParsedMetadata}) object.
 * 
 * <p>
 * This service constructs prompts, calls the AI API, processes responses, and 
 * validates the generated diagrams. It supports generating process flow and 
 * API endpoint diagrams in Mermaid format.
 * </p>
 * 
 * <p>
 * The service uses resilience patterns to handle transient failures when
 * communicating with the AI API.
 * </p>
 * 
 * <p>
 * Dependencies are injected via constructor injection.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * AIAgentService aiAgentService = new AIAgentService(...);
 * ParsedMetadata metadata = ...;
 * MermaidDiagram diagram = aiAgentService.generateProcessFlowDiagram(metadata);
 * }</pre>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-23
 */
@Slf4j
@Service
public class AIAgentService {

    private final WebClient webClient;
    private final AppConfig.AIProperties aiProperties;
    private final PromptBuilder promptBuilder;
    private final MermaidValidator mermaidValidator;

    public AIAgentService(
            WebClient webClient,
            AppConfig.AIProperties aiProperties,
            PromptBuilder promptBuilder,
            MermaidValidator mermaidValidator) {
        this.webClient = webClient;
        this.aiProperties = aiProperties;
        this.promptBuilder = promptBuilder;
        this.mermaidValidator = mermaidValidator;
    }

    /**
     * Generates process flow diagrams from the given {@link ParsedMetadata}
     * object using an AI agent.
     * 
     * @param metadata the parsed metadata containing information for diagram
     *                 generation
     * @return the generated process flow diagrams as a {@link MermaidDiagram}
     *         object.
     * @throws AIServiceException if there is an error during diagram generation
     */
    @Retry(name = "aiService", fallbackMethod = "generateDiagramFallback")
    public MermaidDiagram generateProcessFlowDiagram(ParsedMetadata metadata) {
        log.info("Generating process flow diagram for: {}", metadata.getProcessName());
        long startTime = System.currentTimeMillis();

        try {
            String prompt = promptBuilder.buildProcessFlowPrompt(metadata);
            String diagramCode = callAI(prompt);
            diagramCode = cleanDiagramCode(diagramCode);

            MermaidDiagram diagram = MermaidDiagram.builder()
                    .diagramCode(diagramCode)
                    .type(MermaidDiagram.DiagramType.FLOWCHART)
                    .title(metadata.getProcessName() + " - Process Flow")
                    .build();

            ValidationResult validation = mermaidValidator.validate(diagram);
            diagram.setValidated(validation.isValid());
            diagram.setValidationMessage(
                    validation.isValid() ? "Valid" : String.join(", ", validation.getErrors()));

            long duration = System.currentTimeMillis() - startTime;
            log.info("Generated process flow diagram in {} ms", duration);

            if (!validation.isValid()) {
                log.warn("Diagram validation failed: {}", diagram.getValidationMessage());
            }

            return diagram;
        } catch (AIServiceException e) {
            log.error("Failed to generate diagram", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during diagram", e);
            throw new AIServiceException("Failed to generate diagram", e);
        }
    }

    /**
     * Calls the AI service with the given prompt and returns the generated diagram
     * code.
     * 
     * <p>
     * </p>
     * 
     * @param prompt the prompt to send to the AI service
     * @return the generated diagram code as a String
     * @throws AIServiceException if there is an error during the API call
     */
    private String callAI(String prompt) {
        log.debug("Calling AI API with prompt length: {}", prompt.length());
        AIRequest request = AIRequest.builder()
                .model(aiProperties.getModel())
                .maxTokens(4096)
                .temperature(0.3)
                .messages(List.of(
                        AIRequest.Message.builder()
                                .role("user")
                                .content(prompt)
                                .build()))
                .build();

        try {
            AIResponse response = webClient.post()
                    .uri(aiProperties.getApiUrl())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("x-api-key", aiProperties.getApiKey())
                    .header("User-Agent", "IICSDoc-AI-Agent/1.0")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AIResponse.class)
                    .timeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()))
                    .block();

            if (response == null || response.getContent() == null) {
                throw new AIServiceException("AI API returned empty response");
            }

            String content = response.getTextContent();
            if (response.getUsage() != null) {
                log.info("AI API usage - Input tokens: {}, Output tokens: {}",
                        response.getUsage().getInputTokens(),
                        response.getUsage().getOutputTokens());
            }

            return content;
        } catch (WebClientResponseException e) {
            log.error("AI API error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AIServiceException("AI API error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Failed to call AI API", e);
            throw new AIServiceException("Failed to call AI API", e);
        }
    }

    /**
     * Generate API endpoint diagram code from an AI response.
     * 
     * @param metadata the parsed metadata for which to generate the diagram
     * @return cleaned diagram code
     * @throws AIServiceException if the diagram code is invalid
     */
    @Retry(name = "aiService", fallbackMethod = "generateDiagramFallback")
    public MermaidDiagram generateApiEndpointDiagram(ParsedMetadata metadata) {
        log.info("Generating API endpoint diagram for: {}", metadata.getProcessName());
        String prompt = promptBuilder.buildAPIEndpointPrompt(metadata);

        if (prompt == null) {
            log.info("No API endpoints found, skipping diagram generation");
            return null;
        }

        try {
            String diagramCode = callAI(prompt);
            diagramCode = cleanDiagramCode(diagramCode);

            MermaidDiagram diagram = MermaidDiagram.builder()
                    .diagramCode(diagramCode)
                    .type(MermaidDiagram.DiagramType.SEQUENCE)
                    .title(metadata.getProcessName() + " - API Endpoints")
                    .build();

            ValidationResult validation = mermaidValidator.validate(diagram);
            diagram.setValidated(validation.isValid());
            diagram.setValidationMessage(
                    validation.isValid() ? "Valid" : String.join(", ", validation.getErrors()));

            return diagram;
        } catch (Exception e) {
            log.error("Failed to generate API endpoint diagram", e);
            throw new AIServiceException("Failed to generate API diagram", e);
        }
    }

    /**
     * Clean diagram code by removing markdown fences and extra whitespace.
     * 
     * <p>
     * Removes any leading or trailing code fences ({@code ```mermaid} and
     * {@code ```})
     * and trims whitespace from the diagram code.
     * </p>
     * 
     * @param code the raw diagram code from AI
     * @return cleaned diagram code
     */
    private String cleanDiagramCode(String code) {
        if (code == null) {
            return "";
        }

        code = code.replaceAll("```mermaid\\s*", "");
        code = code.replaceAll("```\\s*$", "");
        code = code.replaceAll("^```\\s*", "");
        code = code.trim();

        return code;
    }

    /**
     * Fallback method for diagram generation when all retries are exhausted.
     * 
     * <p>
     * This method generates an error diagram indicating that the diagram generation
     * could not be completed through the AI service.
     * </p>
     * 
     * @param metadata the parsed metadata for which diagram generation was
     *                 attempted
     * @param e        the exception that caused the failure
     * @return a fallback {@link MermaidDiagram} indicating an error
     */
    private MermaidDiagram generateDiagramFallback(ParsedMetadata metadata, Exception e) {
        log.error("All retries exhausted for diagram generation", e);
        String fallbackCode = String.format(
                "flowchart TD\n" +
                        "   Start[%s]\n" +
                        "   Error[Diagram Generation Failed]\n" +
                        "   Start --> Error\n" +
                        "   style Error fill:#f99,stroke:#f00",
                metadata.getProcessName());

        return MermaidDiagram.builder()
                .diagramCode(fallbackCode)
                .type(MermaidDiagram.DiagramType.FLOWCHART)
                .title(metadata.getProcessName() + " - Process Flow (Error)")
                .validated(false)
                .validationMessage("Fallback diagram - AI service failed: " + e.getMessage())
                .build();
    }
}
