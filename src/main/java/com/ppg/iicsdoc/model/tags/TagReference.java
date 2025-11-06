package com.ppg.iicsdoc.model.tags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a reference to a specific element within a file,
 * such as a line range, XML node, or uniquely identified element. A
 * {@code TagReference} provides the necessary metadata to locate
 * and verify external content in structured documents.
 * 
 * <p>
 * Tag references are commonly used in systems that validate
 * documentation links, synchronize code snippets, or track
 * configuration dependencies. Each reference may include line
 * numbers, XPath expressions, element IDs, or hash values to
 * support flexible and robust identification.
 * </p>
 * 
 * <h2>Example Usage</h2>
 * 
 * <p>
 * The following example demonstrates how a tagging system might
 * define a reference to a specific XML node using XPath:
 * </p>
 * 
 * <pre>{@code
 * TagReference ref = TagReference.xpath(
 *  "src/main/resources/config.xml", 
 *  "/configuration/settings/option");
 * 
 * Tag configTag = Tag.builder()
 *  .id("config.option.timeout")
 *  .type(Tag.TagType.XPATH_REFERENCE)
 *  .label("Timeout setting")
 *  .description("Verifies that the timeout option is correctly documented")
 *  .reference(ref)
 *  .expectedContent("<option name=\"timeout\">30</option>)
 *  .status(Tag.TagStatus.NOT_VERIFIED)
 *  .lastVerified(LocalDateTime.now())
 *  .build();
 * }</pre>
 * 
 * <p>
 * References like these can be periodically validated to ensure
 * that the documentation or configuration remains consistent with the
 * source file.
 * </p>
 * 
 * @see Tag
 * @see Tag.TagType
 * @see Tag.TagStatus
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagReference {

    /** The path to the file containing the referenced element. */
    private String filePath;

    /** The starting line number of referenced content (inclusive). */
    private Integer startLine;

    /** The ending line number of the referenced content (inclusive). */
    private Integer endLine;

    /** An XPath expression identifying the referenced XML node. */
    private String xpath;

    /** A unique element ID within the file, if applicable. */
    private String elementId;

    /** Optional hash value representing the referenced content. */
    private String hash;

    /**
     * Creates a {@code TagReference} that only specifies the file path.
     * 
     * @param filePath the path of the file
     * @return a new {@code TagReference} instance
     */
    public static TagReference file(String filePath) {
        return TagReference.builder()
                .filePath(filePath)
                .build();
    }

    /**
     * Creates a {@code TagReference} that identifies a tag by line range.
     * 
     * @param filePath  the path of the file
     * @param startLine the starting line number (inclusive)
     * @param endLine   the ending line number (inclusive)
     * @return a new {@code TagReference} instance
     */
    public static TagReference lines(
            String filePath,
            int startLine,
            int endLine) {
        return TagReference.builder()
                .filePath(filePath)
                .startLine(startLine)
                .endLine(endLine)
                .build();
    }

    /**
     * Creates a {@code TagReference} that identifies a tag using an 
     * XPath expression.
     * 
     * @param filePath the path of the file 
     * @param xpath    the XPath expression
     * @return a new {@code TagReference} instance
     */
    public static TagReference xpath(String filePath, String xpath) {
        return TagReference.builder()
                .filePath(filePath)
                .xpath(xpath)
                .build();
    }

    /**
     * Creates a {@code TagReference} that identifies a tag by its
     * element ID.
     * 
     * @param filePath  the path of the file
     * @param elementId the element ID
     * @return a new {@code TagReference} instance
     */
    public static TagReference elementId(String filePath, String elementId) {
        return TagReference.builder()
                .filePath(filePath)
                .elementId(elementId)
                .build();
    }
}
