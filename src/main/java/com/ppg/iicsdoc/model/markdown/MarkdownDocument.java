package com.ppg.iicsdoc.model.markdown;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing a Markdown document with its properties.
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkdownDocument {
    
    /** Filename for the markdown document */
    private String filename;

    /** Document's title */
    private String title;

    /** Content to be stored in the document */
    private String content;

    /** Timestamp when the document was generated */
    private LocalDateTime generatedAt;

    /** Document Metadata */
    private DocumentMetadata metadata;

    /**
     * Get file size in bytes.
     * 
     * @return size of the content in {@link Long} bytes
     */
    public long getSize() {
        return content != null ? content.getBytes().length : 0;
    }

    /**
     * Check if the document is empty.
     * 
     * @return {@code true} if content is null or empty, {@code false} otherwise
     */
    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }
}
