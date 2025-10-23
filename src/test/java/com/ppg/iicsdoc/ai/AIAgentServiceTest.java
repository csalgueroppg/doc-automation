package com.ppg.iicsdoc.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.ppg.iicsdoc.config.AppConfig;
import com.ppg.iicsdoc.fixtures.ParsedMetadataFixtures;
import com.ppg.iicsdoc.model.ai.AIResponse;
import com.ppg.iicsdoc.model.ai.MermaidDiagram;
import com.ppg.iicsdoc.model.common.ValidationResult;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.domain.ProcessType;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AIAgentServiceTest {

    @Mock
    private WebClient webClient;

    @Mock 
    private WebClient.RequestBodyUriSpec requestUriSpec;

    @Mock 
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock 
    private WebClient.RequestHeadersSpec requestHeaderSpec;

    @Mock 
    private WebClient.ResponseSpec responseSpec;

    @Mock 
    private MermaidValidator mermaidValidator;

    private AIAgentService aiAgentService = null;
    private PromptBuilder promptBuilder = null;
    private AppConfig.AIProperties aiProperties = new AppConfig.AIProperties();

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder();

        aiProperties.setApiUrl("https://fake-api-agent-lol");
        aiProperties.setApiKey("test-api-key");
        aiProperties.setModel("dummy-agent");
        aiProperties.setTimeoutSeconds(30);

        aiAgentService = new AIAgentService(
            webClient,
            aiProperties,
            promptBuilder,
            mermaidValidator
        );
    }

    @Test
    void shouldGenerateProcessFlowDiagram() {
        ParsedMetadata metadata = ParsedMetadataFixtures.createSimpleMetadata();
        String mockDiagramCode = """
                flowchart TD 
                    Start[Start] --> Process[Process]
                    Process --> End[End]
                """;

        AIResponse mockResponse = createMockAIResponse(mockDiagramCode);

        when(webClient.post()).thenReturn(requestUriSpec);
        when(requestUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeaderSpec);
        when(requestHeaderSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AIResponse.class)).thenReturn(Mono.just(mockResponse));
        when(mermaidValidator.validate(any())).thenReturn(ValidationResult.valid());

        MermaidDiagram diagram = aiAgentService.generateProcessFlowDiagram(metadata);
        assertNotNull(diagram);
        assertTrue(diagram.hasContent());
        assertEquals(MermaidDiagram.DiagramType.FLOWCHART, diagram.getType());
        assertTrue(diagram.isValidated());

        verify(webClient, times(1)).post();
        verify(mermaidValidator, times(1)).validate(any());
    }

    @Test 
    void shouldGenerateAPIEndpointDiagram() {
        ParsedMetadata metadata = ParsedMetadataFixtures.createSimpleMetadata();
        String mockDiagramCode = """
                sequenceDiagram 
                    Client->>Server: GET /customers
                    Server-->>Client: 200 OK
                """;

        AIResponse mockResponse = createMockAIResponse(mockDiagramCode);

        when(webClient.post()).thenReturn(requestUriSpec);
        when(requestUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeaderSpec);
        when(requestHeaderSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AIResponse.class)).thenReturn(Mono.just(mockResponse));
        when(mermaidValidator.validate(any())).thenReturn(ValidationResult.valid());

        MermaidDiagram diagram = aiAgentService.generateApiEndpointDiagram(metadata);
        assertNotNull(diagram);
        assertTrue(diagram.hasContent());
        assertEquals(MermaidDiagram.DiagramType.SEQUENCE, diagram.getType());
    }

    @Test 
    void shouldReturnNullWhenNoAPIEndpoints() {
        ParsedMetadata metadata = ParsedMetadata.builder() 
            .processName("Test Process")
            .processType(ProcessType.CAI)
            .build();

        MermaidDiagram diagram = aiAgentService.generateApiEndpointDiagram(metadata);
        assertNull(diagram);
        verify(webClient, never()).post();
    }

    @Test 
    void shouldCleanMarkdownFences() {
        ParsedMetadata metadata = ParsedMetadataFixtures.createSimpleMetadata();
        String mockDiagramCodeWithFences = """
            ```mermaid
                flowchart TD
                    Start[Start] --> End[End]
            ```
        """;

        AIResponse mockResponse = createMockAIResponse(mockDiagramCodeWithFences);

        when(webClient.post()).thenReturn(requestUriSpec);
        when(requestUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeaderSpec);
        when(requestHeaderSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AIResponse.class)).thenReturn(Mono.just(mockResponse));
        when(mermaidValidator.validate(any())).thenReturn(ValidationResult.valid());

        MermaidDiagram diagram = aiAgentService.generateProcessFlowDiagram(metadata);
        assertNotNull(diagram);
        assertFalse(diagram.getDiagramCode().contains("```"));
        assertTrue(diagram.getDiagramCode().contains("flowchart TD"));
    }

    // Helper method to create a mock AIResponse
    private AIResponse createMockAIResponse(String textContent) {
        AIResponse response = new AIResponse();
        response.setId("msg_123");
        response.setType("message");
        response.setRole("assistant");
        response.setModel("claude-3-5-sonnet-20241022");

        AIResponse.Content content = new AIResponse.Content();
        content.setType("text");
        content.setText(textContent);
        response.setContent(List.of(content));

        AIResponse.Usage usage = new AIResponse.Usage();
        usage.setInputTokens(100);
        usage.setOutputTokens(200);
        response.setUsage(usage);

        return response;
    }
}
