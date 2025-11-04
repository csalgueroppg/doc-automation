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

@Slf4j
@Component
public class TagRenderer {
    
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

    private void renderXPathResult(Tag tag, StringBuilder rendered) {
        rendered.append("**XPath**: `").append(tag.getReference().getXpath())
            .append("`\n\n");

        if (tag.getActualContent() != null) {
            rendered.append("**Result**: `").append(tag.getActualContent())
                .append("`\n\n");
        }
    }

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

    private void renderGenericTag(Tag tag, StringBuilder rendered) {
        if (tag.getDescription() != null) {
            rendered.append(tag.getDescription()).append("\n\n");
        }
    }

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
