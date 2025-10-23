package com.ppg.iicsdoc.model.markdown;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing a section within a markdown document.
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSection {
    
    /** Section title */
    private String title;

    /** Header level (matching html h1-h6) */
    private int level;

    /** Section content data */
    private String content;

    /** Include in table of contents */
    private boolean includeInToc;

    /**
     * Get markdown header representation.
     * 
     * @return formatted markdown header string
     */
    public String getHeader() {
        return "#".repeat(Math.max(1, Math.min(6, level))) + " " + title;
    }
}
