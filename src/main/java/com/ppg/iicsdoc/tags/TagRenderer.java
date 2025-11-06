package com.ppg.iicsdoc.tags;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.model.tags.Tag;
import com.ppg.iicsdoc.model.tags.TagReference;

import lombok.extern.slf4j.Slf4j;

/**
 * Renders {@link Tag} objects into human-readable markdown content.
 * 
 * <p>
 * The {@code TagRenderer} is responsible for converting tag metadata and
 * verification results into formatted markdown blocks, suitable for embedding
 * in documentation, reports, or dashboards.
 * </p>
 * 
 * <h2>Rendering Behavior</h2>
 * <ul>
 * <li>Displays tag label as a bold heading</li>
 * <li>Wraps tag details in collapsible section</li>
 * <li>Renders code snippets, XPath results, or file metadata based on tag
 * type</li>
 * <li>Appends verification status with appropriate icons</li>
 * </ul>
 * 
 * <h2>Example Output</h2>
 * 
 * <pre>
 * **User Creation Method**
 * 
 * &lt;details&gt;
 * &lt;summary&gt;Referenced from: src/UserService.java&lt;/summary&gt;
 * 
 * ```xml
 * public User createUser(UserRequest request) { ... }
 * ```
 *
 * *Lines 42-55*
 *
 * *Status: ✅ VALID (Last verified: 2025-11-04T12:34:56)*
 * &lt;/details&gt;
 * </pre>
 * 
 * @see Tag
 * @see TagReference
 * @see TaggedDocument
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-04
 */
@Slf4j
@Component
public class TagRenderer {

    /**
     * Renders a single {@link Tag} into markdown format.
     * 
     * @param tag the tag to render
     * @return a markdown-formatted string representing the tag
     */
    public String renderTag(Tag tag) {
        StringBuilder rendered = new StringBuilder();
        if (tag.getLabel() != null) {
            rendered.append("**").append(tag.getLabel()).append("**\n\n");
        }

        rendered.append("<details>\n");
        rendered.append("<summary>Referenced from: ")
                .append(tag.getReference().getFilePath())
                .append("</summary>\n\n");

        switch (tag.getType()) {
            case CODE_SNIPPET, LINE_REFERENCE -> renderCodeSnippet(tag, rendered);
            case XPATH_REFERENCE -> renderXPathResult(tag, rendered);
            case FILE_REFERENCE -> renderFileInfo(tag, rendered);
            default -> renderGenericTag(tag, rendered);
        }

        renderVerificationStatus(tag, rendered);
        rendered.append("</details>\n\n");

        return rendered.toString();
    }

    /**
     * Renders a code snippet or line reference tag.
     * 
     * @param tag      the tag to render
     * @param rendered the output buffer
     */
    private void renderCodeSnippet(Tag tag, StringBuilder rendered) {
        if (tag.getActualContent() != null) {
            rendered.append("```xml\n");
            rendered.append(tag.getActualContent());
            rendered.append("\n```\n\n");

            TagReference ref = tag.getReference();
            if (ref.getStartLine() != null) {
                rendered.append("*Lines ").append(ref.getStartLine())
                        .append("-").append(ref.getEndLine()).append("*\n\n");
            }
        }
    }

    /**
     * Renders an XPath reference tag.
     * 
     * @param tag      the tag to render
     * @param rendered the output buffer
     */
    private void renderXPathResult(Tag tag, StringBuilder rendered) {
        rendered.append("**XPath**: `").append(tag.getReference().getXpath())
                .append("`\n\n");

        if (tag.getActualContent() != null) {
            rendered.append("**Result**: `").append(tag.getActualContent())
                    .append("`\n\n");
        }
    }

    /**
     * Renders file metadata for a file reference tag.
     * 
     * @param tag      the tag to render
     * @param rendered the output buffer
     */
    private void renderFileInfo(Tag tag, StringBuilder rendered) {
        try {
            Path filePath = Paths.get(tag.getReference().getFilePath());
            if (Files.exists(filePath)) {
                long size = Files.size(filePath);
                rendered.append("**File Size**: ").append(size).append(" bytes\n\n");
            }
        } catch (IOException e) {
            log.warn("Could not read file info", e);
        }
    }

    /**
     * Renders a generic tag with description.
     * 
     * @param tag      the tag to render
     * @param rendered the output buffer
     */
    private void renderGenericTag(Tag tag, StringBuilder rendered) {
        if (tag.getDescription() != null) {
            rendered.append(tag.getDescription()).append("\n\n");
        }
    }

    /**
     * Appends verification status to the rendered output.
     * 
     * @param tag      the tag to render
     * @param rendered the output buffer
     */
    private void renderVerificationStatus(Tag tag, StringBuilder rendered) {
        String statusIcon = switch (tag.getStatus()) {
            case VALID -> "✅";
            case OUTDATED -> "⚠️";
            case MISSING, ERROR -> "❌";
            case NOT_VERIFIED -> "❓";
        };

        rendered.append("*Status: ").append(statusIcon).append(" ")
                .append(tag.getStatus()).append("*");

        if (tag.getLastVerified() != null) {
            rendered.append(" (Last verified: ")
                    .append(tag.getLastVerified()).append(")");
        }

        rendered.append("\n");
    }

    /**
     * Replaces all tag annotations in the document content with their 
     * rendered markdown.
     * 
     * @param content the original document content
     * @param tags    the list of tags to render and replace
     * @return the content with rendered tags replacing original annotations
     */
    public String replaceTagsWithRendered(String content, List<Tag> tags) {
        String result = content;
        for (Tag tag : tags) {
            String tagPattern = String.format("\\[iics:%s\\]\\([^)]+\\)",
                    tag.getType().name().toLowerCase());
            String rendered = renderTag(tag);

            result = result.replaceFirst(tagPattern, rendered);
        }

        return result;
    }
}
