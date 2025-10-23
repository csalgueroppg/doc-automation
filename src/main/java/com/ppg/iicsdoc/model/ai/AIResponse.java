package com.ppg.iicsdoc.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * Represents the response from an AI model after processing a request.
 * 
 * <p>
 * This class includes metadata such as the response ID, model used, role,
 * content, stop reason, and token usage statistics.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @date 2025-10-21
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIResponse {
     
    /** Unique identifier for the response. */
    private String id;

    /** Type of the response object. */
    private String type;

    /** Role of the responder (e.g., "assistant"). */
    private String role;

    /** List of content elements returned by the AI model. */
    private List<Content> content;

    /** The model used to generated the response. */
    private String model;

    /** Reason why the generation stopped (e.g., "length", "stop sequence"). */
    @JsonProperty("stop_reason")
    private String stopReason;

    /** The sequence that caused the generation to stop, if applicable. */
    @JsonProperty("stop_sequence")
    private String stopSequence;

    /** Token usage statistics for the request and response. */
    private Usage usage;

    /**
     * Represents a single content block in the AI response.
     * 
     * <p>
     * Typically includes a type (e.g., "text") and the actual content.
     * </p>
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {

        /** Type of the content (e.g., "text"). */
        private String type;

        /** Textual content required by the model. */
        private String text;
    }

    /**
     * Represents token usage details for the AI interaction
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        /** Number of tokens used in the input prompt. */
        @JsonProperty("input_tokens")
        private Integer inputTokens;

        /** Number of tokens generated in the output. */
        @JsonProperty("output_tokens")
        private Integer outputTokens;
    }

    /**
     * Gets the text content from the response
     * 
     * @return a {@code String} with the corresponding content.
     */
    public String getTextContent() {
        if (content == null || content.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Content c : content) {
            if ("text".equals(c.getType())) {
                sb.append(c.getText());
            }
        }

        return sb.toString();
    }
}
