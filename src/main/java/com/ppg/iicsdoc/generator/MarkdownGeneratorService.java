package com.ppg.iicsdoc.generator;

import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ppg.iicsdoc.model.ai.MermaidDiagram;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.markdown.DocumentMetadata;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;

import freemarker.template.Configuration;
import freemarker.template.Template;
import com.ppg.iicsdoc.exception.ValidationException;

/**
 * Generates markdown documentation based on parsed metadata and Mermaid
 * diagram.
 * 
 * <p>
 * This class provides functionality to validate input data, prepare template
 * models, render markdown content using {@link FreeMarker} templates, and
 * construct {@link MarkdownDocument} instances enriched with metadata.
 * </p>
 * 
 * <p>
 * It supports generating documents with both process flow and API diagrams, and
 * includes helper methods for input validation, template rendering, filename
 * generation, and metadata construction.
 * </p>
 * 
 * <h2>Usage Example:</h2>
 * 
 * <pre>{@code
 * 
 * MarkdownGeneratorService generator = new MarkdownGeneratorService();
 * MarkdownDocument doc = generator.generate(parsedMetadata, processFlowDiagram, apiDiagram);
 * }</pre>
 * 
 * <p>
 * This class is intended to be used as part of a documentation pipeline for
 * IICS processes.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-23
 */
@Slf4j
@Service
public class MarkdownGeneratorService {

    /** Service configuration */
    private final Configuration freemarkerConfig;

    public MarkdownGeneratorService(Configuration freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    /**
     * Generates a {@link MarkdownDocument} based on the provided metadata
     * and Mermaid diagrams.
     * 
     * <p>
     * This method validates the input metadata and process flow diagram, prepares
     * the template data, renders the markdown content using a predefined template,
     * and constructs a {@code MarkdownDocument} with the generated content and
     * associated metadata.
     * </p>
     * 
     * @param metadata           the parsed metadata containing process details;
     *                           must not be {@code null}
     * @param processFlowDiagram the Mermaid diagram representing the process flow;
     *                           must not be {@code null}
     * @param apiDiagram         the Mermaid diagram representing the API structure;
     *                           may be {@code null}
     * @return a fully constructed {@link MarkdownDocument} containing the rendered
     *         markdown content
     * @throws ValidationException if input validation fails or document generation
     *                             encounters an error
     */
    public MarkdownDocument generate(
            ParsedMetadata metadata,
            MermaidDiagram processFlowDiagram,
            MermaidDiagram apiDiagram) {
        log.info("Generating markdown document for: {}", metadata.getProcessName());
        long startTime = System.currentTimeMillis();

        try {
            validateInputs(metadata, processFlowDiagram);

            Map<String, Object> templateData = prepareTemplateData(
                    metadata,
                    processFlowDiagram,
                    apiDiagram);

            String content = renderTemplate("markdown-template.flt", templateData);
            MarkdownDocument document = MarkdownDocument.builder()
                    .filename(generateFileName(metadata))
                    .title(metadata.getProcessName())
                    .content(content)
                    .generatedAt(LocalDateTime.now())
                    .metadata(createDocumentMetadata(metadata))
                    .build();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Generated markdown document ({} bytes) in {} ms",
                    document.getSize(), duration);

            return document;
        } catch (Exception e) {
            log.error("Failed to generate markdown document: " + e.getMessage(), e);
            throw new ValidationException(
                    "Failed to generate markdown document: " + e.getMessage(),
                    List.of(e.getMessage()));
        }
    }

    /**
     * Generates a {@link MarkdownDocument} using the provided metadata and process
     * flow diagram. This method is a convenience overload that does not require an
     * API diagram.
     * 
     * <p>
     * This is a convenience method that delegates to
     * {@link #generate(ParsedMetadata, MermaidDiagram, MermaidDiagram)} with a
     * {@code null} API diagram.
     * </p>
     * 
     * @param metadata           the parsed metadata containing process details;
     *                           must not be {@code null}
     * @param processFlowDiagram the Mermaid diagram representing the process flow;
     *                           must not be {@code null}
     * @return a fully constructed {@link MarkdownDocument} containing the rendered
     *         markdown content
     * @throws ValidationException if input validation fails or document generation
     *                             encounters an error
     */
    public MarkdownDocument generate(
            ParsedMetadata metadata,
            MermaidDiagram processFlowDiagram) {
        return generate(metadata, processFlowDiagram, null);
    }

    /**
     * Validate the required inputs for markdown document generation.
     * 
     * <p>
     * This method checks that the {@code metadata} and {@code processFlowDiagram}
     * are not {@code null}, and that the metadata contains a non-empty process name
     * and a defined process type. If also verifies that the process flow diagram
     * has content.
     * </p>
     * 
     * @param metadata           the parsed metadata to validate; may not be
     *                           {@code null}
     * @param processFlowDiagram the Mermaid diagram representing the process flow;
     *                           may not be {@code null}
     * @throws ValidationException if any required input is missing or invalid
     */
    private void validateInputs(ParsedMetadata metadata, MermaidDiagram processFlowDiagram) {
        List<String> errors = new ArrayList<>();
        if (metadata == null) {
            errors.add("ParsedMetadata is required.");
        } else {
            if (metadata.getProcessName() == null || metadata.getProcessName().isEmpty()) {
                errors.add("Process name is required in ParsedMetadata.");
            }

            if (metadata.getProcessType() == null) {
                errors.add("Process type is required in ParsedMetadata.");
            }
        }

        if (processFlowDiagram == null || !processFlowDiagram.hasContent()) {
            errors.add("Process flow diagram is required");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Invalid inputs for markdown generation",
                    errors);
        }
    }

    /**
     * Prepares the data model used to render the markdown template.
     * 
     * <p>
     * This method extracts relevant information from the provided
     * {@link ParsedMetadata}, {@link MermaidDiagram} instances, and the current
     * timestamp, organizing it into a map that can be used to populate the markdown
     * tempalte.
     * </p>
     * 
     * <p>
     * The returned map may include keys such as:
     * </p>
     *
     * <ul>
     * <li>{@code processName}</li>
     * <li>{@code processType}</li>
     * <li>{@code version}</li>
     * <li>{@code description}</li>
     * <li>{@code author}</li>
     * <li>{@code created}</li>
     * <li>{@code modified}</li>
     * <li>{@code connections}</li>
     * <li>{@code transformations}</li>
     * <li>{@code openApiEndpoints}</li>
     * <li>{@code dataFlow}</li>
     * <li>{@code processFlowDiagram}</li>
     * <li>{@code apiDiagram}</li>
     * <li>{@code generatedAt}</li>
     * </ul>
     * 
     * @param metadata           the parsed metadata containing process details;
     *                           must not be {@code null}
     * @param processFlowDiagram the Mermaid diagram representing the process flow;
     *                           may be {@code null}
     * @param apiDiagram         the Mermaid diagram representing the API structure;
     *                           may be {@code null}
     * @return a map of template data to be used for rendering the markdown document
     */
    private Map<String, Object> prepareTemplateData(
            ParsedMetadata metadata,
            MermaidDiagram processFlowDiagram,
            MermaidDiagram apiDiagram) {
        Map<String, Object> data = new HashMap<>();

        data.put("processName", metadata.getProcessName());
        data.put("processType", metadata.getProcessType().getDisplayName());
        data.put("version", metadata.getVersion() != null ? metadata.getVersion() : "1.0");

        if (metadata.getDescription() != null) {
            data.put("description", metadata.getDescription());
        }

        if (metadata.getAuthor() != null) {
            data.put("author", metadata.getAuthor());
        }

        if (metadata.getCreated() != null) {
            data.put("created", metadata.getCreated());
        }

        if (metadata.getModified() != null) {
            data.put("modified", metadata.getModified());
        }

        if (metadata.getConnections() != null && !metadata.getConnections().isEmpty()) {
            data.put("connections", metadata.getConnections());
        }

        if (metadata.getTransformations() != null && !metadata.getTransformations().isEmpty()) {
            data.put("transformations", metadata.getTransformations());
        }

        if (metadata.getOpenApiEndpoints() != null && !metadata.getOpenApiEndpoints().isEmpty()) {
            data.put("openApiEndpoints", metadata.getOpenApiEndpoints());
        }

        if (metadata.getDataFlow() != null) {
            data.put("dataFlow", metadata.getDataFlow());
        }

        if (processFlowDiagram != null && processFlowDiagram.hasContent()) {
            data.put("processFlowDiagram", processFlowDiagram.toMarkdown());
        }

        if (apiDiagram != null && apiDiagram.hasContent()) {
            data.put("apiDiagram", apiDiagram.toMarkdown());
        }

        data.put("generatedAt", LocalDateTime.now());
        return data;
    }

    /**
     * Renders a markdown template using the provided data model.
     * 
     * <p>
     * This method loads the specified {@link FreeMarker} template and processes
     * it with the given data, returning the resulting markdown content as a string.
     * </p>
     * 
     * @param templateName the name of the FreeMarker template to render.
     * @param data         the data model to populate the template; must not be
     *                     {@code null}
     * @return the rendered markdown content as a string
     * @throws Exception if an error occurs during template processing
     */
    private String renderTemplate(String templateName, Map<String, Object> data) throws Exception {
        Template template = freemarkerConfig.getTemplate(templateName);
        StringWriter writer = new StringWriter();

        template.process(data, writer);
        return writer.toString();
    }

    /**
     * Generates a sanitized markdown filename based on the process name in the
     * metadata.
     * 
     * <p>
     * The process name is converted to lowercase, non-alphanumeric characters are
     * replaced with hyphens, and leading/trailing hyphens are removed. The
     * resulting filename is suffixed with {@code .md} extension.
     * </p>
     * 
     * @param metadata the parsed metadata containing the process name; must not be
     *                 {@code null}
     * @return a sanitized markdown filename
     */
    private String generateFileName(ParsedMetadata metadata) {
        String baseName = metadata.getProcessName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        return baseName + ".md";
    }

    /**
     * Constructs a {@link DocumentMetadata} instance from the provided
     * 
     * @{link ParsedMetadata}.
     * 
     *        <p>
     *        This method extracts relevant metadata fields such as process name,
     *        version,
     *        author, and any additional custom properties, packaging them into a
     *        {@code DocumentMetadata} object.
     *        </p>
     * 
     * @param metadata the parsed metadata containing process details; must not be
     *                 {@code null}
     * @return a {@code DocumentMetadata} instance populated with data from the
     *         provided metadata
     */
    private DocumentMetadata createDocumentMetadata(ParsedMetadata metadata) {
        return DocumentMetadata.builder()
                .processName(metadata.getProcessName())
                .processVersion(metadata.getVersion())
                .author(metadata.getAuthor())
                .generatedBy("IICS Documentation Generator v1.0.0")
                .customMetadata(metadata.getAdditionalProperties())
                .build();
    }
}
