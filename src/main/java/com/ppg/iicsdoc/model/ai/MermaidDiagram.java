package com.ppg.iicsdoc.model.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Mermaid diagram with its associated metadata and utilities.
 * 
 * <p>
 * This class encapsulates the Mermaid syntax code, diagram type, title, and
 * validation status. It also provides utility methods for checking content
 * and generating Markdown-formatted output.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MermaidDiagram {

    /** Mermaid syntax code representing the diagram */
    private String diagramCode;

    /** Type of the Mermaid diagram */
    private DiagramType type;

    /** Title string for the diagram */
    private String title;

    /** Indicates whether the diagram has been validated */
    private boolean validated;

    /** Message resulting from the diagram's validation process. */
    private String validationMessage;

    /**
     * Enumeration of supported Mermaid diagram types.
     */
    public enum DiagramType {
        FLOWCHART("flowchart"),
        SEQUENCE("sequenceDiagram"),
        CLASS("classDiagram"),
        STATE("stateDiagram"),
        ER("erDiagram"),
        GANTT("gantt");

        /** The Mermaid keyword associated with the diagram type. */
        private final String mermaidKeyword;

        /**
         * Constructs a {@code DiagramType} with the specified Mermaid keyword.
         *
         * @param mermaidKeyword the Mermaid keyword representing the diagram type.
         */
        DiagramType(String mermaidKeyword) {
            this.mermaidKeyword = mermaidKeyword;
        }

        /**
         * Returns the Mermaid keyword associated with this diagram type.
         * 
         * @return the Mermaid keyword
         */
        public String getMermaidKeyboard() {
            return mermaidKeyword;
        }
    }

    /**
     * Checks whether the diagram contains non-empty Mermaid syntax code.
     *
     * @return {@code true} if the diagram code is not null and not empty;
     *         {@code false} otherwise
     */
    public boolean hasContent() {
        return diagramCode != null && !diagramCode.trim().isEmpty();
    }

    /**
     * Returns the Mermaid diagram code formatted as a Markdown code block.
     *
     * @return the Markdown-formatted Mermaid diagram code, or an empty string if no
     *         content is present
     */
    public String toMarkdown() {
        if (!hasContent()) {
            return "";
        }

        return "```mermaid\n" + diagramCode + "\n````";
    }
}
