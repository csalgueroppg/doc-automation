package com.ppg.iicsdoc.ai;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.model.domain.Connection;
import com.ppg.iicsdoc.model.domain.DataFlow;
import com.ppg.iicsdoc.model.domain.OpenAPIEndpoint;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.domain.Transformation;

import lombok.extern.slf4j.Slf4j;

/**
 * A utility component responsible for constructing prompt strings
 * used to generate Mermaid diagrams from IICS metadata.
 * 
 * <p>
 * This class provides methods to build prompts for various diagram types
 * including:
 * </p>
 * <ul>
 * <li>Process flow diagrams</li>
 * <li>API endpoint sequence diagrams</li>
 * <li>Data lineage flowcharts</li>
 * </ul>
 * 
 * <p>
 * The generated prompts are designed to be consumed by AI models capable
 * of interpreting structured instructions and producing Mermaid syntax
 * diagrams.
 * </p>
 * 
 * <p>
 * Each method in this class accepts a {@link ParsedMetadata} object
 * containing relevant metadata and returns a formatted prompt string
 * tailored to the specific diagram type.
 * </p>
 * 
 * <p>
 * Logging is used to track prompt generation and assist with debugging.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-21
 */
@Slf4j
@Component
public class PromptBuilder {

    /**
     * Builds a prompt string for generating a Mermaid flowchart diagram based
     * on IICS process metadata.
     * 
     * <p>
     * The generated prompt includes structured information about the process
     * such as its name, type, version, description, connections, transformations,
     * and data flow. It also provides detailed instructions for rendering the
     * diagram using Mermaid syntax.
     * </p>
     * 
     * <p>
     * The prompt is intended for use with AI models capable of generating Mermaid
     * diagrams from textual descriptions.
     * </p>
     * 
     * @param metadata the parsed metadata containing details about the IICS
     *                 process, including its connections, transformations,
     *                 and data flow.
     * @return a formatted prompt string instructing the AI to generate a Mermaid
     *         flowchart diagram.
     */
    public String buildProcessFlowPrompt(ParsedMetadata metadata) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(
                """
                You are an expert at creating Mermaid diagrams for data\
                integration processes
                
                Based on the following IICS\
                 (Informatica Intelligent Cloud Services)""");

        prompt.append(metadata.getProcessType()).append(" process metadata, ");
        prompt.append("create a detailed mermaid flowchart diagram");
        prompt.append("## Process Information\n");
        prompt.append(" - **Process Name**: ")
                .append(metadata.getProcessName())
                .append("\n");

        prompt.append(" - **Type**: ")
                .append(metadata.getProcessType().getDisplayName())
                .append("\n");

        prompt.append("- **Version**: ")
                .append(metadata.getVersion())
                .append("\n");

        if (metadata.getDescription() != null) {
            prompt.append("- **Description**: ")
                    .append(metadata.getDescription())
                    .append("\n");
        }

        prompt.append("\n");
        if (metadata.getConnections() != null && !metadata.getConnections().isEmpty()) {
            prompt.append("## Connections\n");
            for (Connection conn : metadata.getConnections()) {
                prompt.append(" - **")
                        .append(conn.getName())
                        .append("** (")
                        .append(conn.getType())
                        .append(")");

                if (conn.getUrl() != null) {
                    prompt.append(": ").append(conn.getUrl());
                }

                prompt.append("\n");
            }

            prompt.append("\n");
        }

        if (metadata.getTransformations() != null && !metadata.getTransformations().isEmpty()) {
            prompt.append("## Transformations\n");
            for (Transformation trans : metadata.getTransformations()) {
                prompt.append(" - **")
                        .append(trans.getName())
                        .append("** (")
                        .append(trans.getType())
                        .append(")");

                if (trans.getExpression() != null) {
                    prompt.append(": ").append(trans.getExpression());
                }

                if (trans.getCondition() != null) {
                    prompt.append(": ").append(trans.getCondition());
                }

                prompt.append("\n");
            }

            prompt.append("\n");
        }

        if (metadata.getDataFlow() != null) {
            prompt.append("## Data Flow\n");
            DataFlow flow = metadata.getDataFlow();

            if (flow.getSource() != null) {
                prompt.append(" - **Source**: ")
                        .append(flow.getSource().getEntity());

                prompt.append(" (Connection: ")
                        .append(flow.getSource().getConnectionRef())
                        .append(")\n");
            }

            if (flow.getTransformationRefs() != null &&
                    !flow.getTransformationRefs().isEmpty()) {
                prompt.append(" - **Transformation Pipeline**: ");
                prompt.append(String.join(" -> ", flow.getTransformationRefs()))
                        .append("\n");
            }

            if (flow.getTarget() != null) {
                prompt.append(" - **Target**: ")
                        .append(flow.getTarget().getEntity());
                prompt.append(" (Connection: ")
                        .append(flow.getTarget().getConnectionRef())
                        .append(")\n");
            }

            prompt.append("\n");
        }

        prompt.append("## Instructions \n");
        prompt.append("Create a Mermaid flowchart (use 'flowchart TD' or 'flowchart LR')" +
                "that:\n");
        prompt.append("1. Shows the complete data flow from source to target\n");
        prompt.append("2. Includes all transformations as processing nodes\n");
        prompt.append("3. Uses appropriate node shapes:\n");
        prompt.append("   - Use [(Database)] for database connections\n");
        prompt.append("   - Use {{API}} for REST/API connections\n");
        prompt.append("   - Use [Process] for transformations\n");
        prompt.append("   - Use {Decision} for filters/conditions\n");
        prompt.append("4. Labels all connections with meaningful descriptions\n");
        prompt.append("5. Uses a clear, logical layout\n");
        prompt.append("6. Includes a title using '---' syntax\n\n");

        prompt.append("Return ONLY the Mermaid diagram code, without any explanation" +
                " or markdown code fences.\n");
        prompt.append("Start directly with 'flowchart' or '---'.\n");

        log.debug("Built process flow prompt with {} characters", prompt.length());
        return prompt.toString();
    }

    /**
     * Builds a prompt string for generating Mermaid sequence diagram based on
     * OpenAPI
     * endpoint metadata extracted from an IICS process.
     * 
     * <p>
     * The prompt includes structured details for each API endpoint such as HTTP
     * method, path, summary, operation ID, parameters, and responses code. It
     * also provides specific instructions for rendering
     * </p>
     * 
     * <p>
     * This prompt is intended for use with AI models capable of generating Mermaid
     * diagrams from textual descriptions.
     * </p>
     * 
     * @param metadata the parsed metadata containing OpenAPI endpoint definitions
     *                 from an IICS process.
     * @return a formatted prompt string instructing the AI to generated Mermaid
     *         sequence diagram, or {@code null} if no endpoints are available.
     */
    public String buildAPIEndpointPrompt(ParsedMetadata metadata) {
        if (metadata.getOpenApiEndpoints() == null ||
                metadata.getOpenApiEndpoints().isEmpty()) {
            return null;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append(
                """
                You are an expert at creating Mermaid diagrams for data\
                integration processes
                
                Based on the following IICS\
                 (Informatica Intelligent Cloud Services)""");

        prompt.append("""
                Based on the following OpenAPIEndpoints from an IICS process, \
                create a Mermaid sequence diagram showing the API interactions.
                
                """);

        prompt.append("## Endpoints\n");
        for (OpenAPIEndpoint endpoint : metadata.getOpenApiEndpoints()) {
            prompt.append("### ")
                    .append(endpoint.getMethod())
                    .append(" ")
                    .append(endpoint.getPath())
                    .append("\n");

            if (endpoint.getSummary() != null) {
                prompt.append(" - **Summary**: ")
                        .append(endpoint.getSummary())
                        .append("\n");
            }

            if (endpoint.getOperationId() != null) {
                prompt.append(" - **Operation**:")
                        .append(endpoint.getOperationId())
                        .append("\n");
            }

            if (endpoint.getParameters() != null &&
                    !endpoint.getParameters().isEmpty()) {
                prompt.append("- **Parameters**: ");
                String params = endpoint.getParameters().stream()
                        .map(p -> p.getName() + " (" + p.getType() + ")")
                        .collect(Collectors.joining(", "));

                prompt.append(params).append("\n");
            }

            if (endpoint.getResponses() != null && !endpoint.getResponses().isEmpty()) {
                prompt.append(" - **Responses**: ");
                prompt.append(String.join(", ", endpoint.getResponses().keySet()))
                        .append("\n");
            }

            prompt.append("\n");
        }

        prompt.append("## Instructions\n");
        prompt.append("Create a Mermaid sequence diagram that:\n");
        prompt.append("1. Shows the client-server interaction for each endpoint\n");
        prompt.append("2. Includes request parameters and response codes\n");
        prompt.append("3. Uses appropriate sequence diagram syntax\n");
        prompt.append("4. Groups related operations if applicable\n\n");

        prompt.append("Return ONLY the Mermaid diagram code, without any explanation or" +
                " markdown code fences");
        prompt.append("Start directly with 'sequenceDiagram'.\n");

        log.debug("Built process flow prompt with {} characters", prompt.length());
        return prompt.toString();
    }

    /**
     * Builds a prompt string for generating a Mermaid flowchart diagram
     * that illustrates the data lineage of an IICS process.
     * 
     * <p>
     * The prompt includes structured information about the source
     * and target systems, entities involved, and any transformations
     * steps applied during the data flow. It also provides specific
     * instructions for rendering the diagram using Mermaid syntax.
     * </p>
     * 
     * <p>
     * This prompt is intended for use with AI models capable of generating
     * Mermaid diagrams from textual descriptions.
     * </p>
     * 
     * @param metadata the parsed metadata containing data flow details of the
     *                 IICS process.
     * @return a Mermaid flowchart diagram, or {@code null} if no data flow
     *         information is available.
     */
    public String buildDataLineagePrompt(ParsedMetadata metadata) {
        if (metadata.getDataFlow() == null) {
            return null;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append(
                """
                You are an expert at creating Mermaid diagrams for data\
                integration processes
                
                Based on the following IICS\
                 (Informatica Intelligent Cloud Services)""");

        prompt.append("""
                Based on the following data flow information, \
                create a Mermaid flowchart showing the data lineage
                
                """);

        DataFlow flow = metadata.getDataFlow();
        prompt.append("## Data Lineage\n");

        if (flow.getSource() != null) {
            prompt.append(" - **Source System**: ")
                    .append(flow.getSource().getConnectionRef())
                    .append("\n");

            prompt.append(" - **Source Entity**: ")
                    .append(flow.getSource().getEntity())
                    .append("\n");
        }

        if (flow.getTarget() != null) {
            prompt.append(" - **Target System**: ")
                    .append(flow.getTarget().getConnectionRef())
                    .append("\n");

            prompt.append(" - **Target Entity**: ")
                    .append(flow.getTarget().getEntity())
                    .append("\n");
        }

        if (flow.getTransformationRefs() != null &&
                !flow.getTransformationRefs().isEmpty()) {
            prompt.append(" - **Transformations Applied**: ");
            prompt.append(String.join(", ", flow.getTransformationRefs()))
                    .append("\n");
        }

        prompt.append("\n## Instructions\n");
        prompt.append("Create a Mermaid flowchart that clearly shows:\n");
        prompt.append("1. The origin of the data (source)\n");
        prompt.append("2. All transformation steps\n");
        prompt.append("3. The final destination (target)\n");
        prompt.append("4. Data quality or validation steps if applicable\n\n");

        prompt.append("Return ONLY the Mermaid diagram code, without any explanation or" +
                " markdown code fences");

        log.debug("Built process flow prompt with {} characters", prompt.length());
        return prompt.toString();
    }
}
