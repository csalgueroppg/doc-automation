package com.ppg.iicsdoc.model.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a request payload for interacting with an AI model.
 * 
 * <p>
 * This class encapsulates the model configuration, input messages, and
 * generation parameters such as token limits and temperature.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * <pre>{@code
 * AIRequest request = AIRequest.builder()
 *      .model("gpt-4")
 *      .maxTokens(1000)
 *      .temperature(0.7)
 *      .messages(List.of(
 *          new AIRequest.Message("user", "Hello, how are you?")
 *       ))
 *      .build();
 * }</pre>
 * 
 * @author Carlos Salguero 
 * @version 1.0.0
 * @since 2025-10-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {

    /** The identifier of the AI model to use */
    private String model;

    /** The maximum number of tokens to generate in the response. */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /** The list of messages forming the conversation history. */
    private List<Message> messages;

    /** 
     * Sampling temperature to use. Higher values (e.g., 1.0) make output
     * more random, while lower values (e.g., 0.2) make it more focused 
     * and deterministic.
     */
    private Double temperature;

    /**
     * Represents a single message in the conversation
     * 
     * <p>
     * Each message includes a role (e.g., "user', "assistant") and the 
     * content of the message.
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class Message {

        /** 
         * The role of the message sender. Common values include:
         * 
         * <ul>
         * <li>{@code "user"} - the end user</li>
         * <li>{@code "assistant"} - the AI model</li>
         * <li>{@code "system"} - instructions or context for the assistant</li>
         * </ul
         */
        private String role;

        /** The textual content of the message. */
        private String content;
    }
}
