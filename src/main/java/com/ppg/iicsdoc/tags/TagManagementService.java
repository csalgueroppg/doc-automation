package com.ppg.iicsdoc.tags;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ppg.iicsdoc.model.tags.Tag;
import com.ppg.iicsdoc.model.tags.TagVerificationResult;
import com.ppg.iicsdoc.model.tags.TaggedDocument;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TagManagementService {
    
    private final TagParser tagParser;
    private final TagVerifier tagVerifier;
    private final TagRenderer tagRenderer;

    public TagManagementService(
        TagParser tagParser,
        TagVerifier tagVerifier,
        TagRenderer tagRenderer) {
        this.tagParser = tagParser;
        this.tagVerifier = tagVerifier;
        this.tagRenderer = tagRenderer;
    }

    public TaggedDocument processDocument(String documentId, String content) {
        log.info("Processing document with tags: {}", documentId);

        TaggedDocument taggedDoc = tagParser.parse(documentId, content);
        tagVerifier.verify(taggedDoc);

        return taggedDoc;
    }

    public String verifyAndUpdateDocument(String content) {
        TaggedDocument taggedDoc = tagParser.parse("temp", content);
        tagVerifier.verify(taggedDoc);

        return tagRenderer.replaceTagsWithRendered(
            taggedDoc.getContent(),
            taggedDoc.getTags());
    }

    public List<Tag> getOutdatedTags(TaggedDocument document) {
        return document.getTags().stream()
            .filter(tag -> tag.getStatus() == Tag.TagStatus.OUTDATED)
            .collect(Collectors.toList());
    }

    public void updateTagContent(Tag tag) {
        try {
            tagVerifier.verifyTag(tag);
        } catch (Exception e) {
            log.error("Failed to update tag content", e);
            tag.setStatus(Tag.TagStatus.ERROR);
        }
    }

    public String generateReport(TagVerificationResult result) {
        StringBuilder report = new StringBuilder();

        report.append("# Tag Verification Report\n\n");
        report.append("## Summary \n\n");
        report.append(String.format("- Total tags: %d\n", result.getTotalTags()));
        report.append(String.format("- ✅ Valid: %d\n", result.getValidTags()));
        report.append(String.format("- ⚠️ Outdated: %d\n", result.getOutdatedTags()));
        report.append(String.format("- ❌ Missing: %d\n", result.getMissingTags()));
        report.append(String.format("- ❌ Error: %d\n", result.getErrorTags()));
        report.append("- **Success Rate**: %.1f%%\n\n".formatted(
                result.getSuccessRate() * 100));

        if (!result.isAllValid()) {
            report.append("## Issues\n\n");

            for (Tag tag : result.getProblematicTags()) {
                report.append("### ").append(tag.getStatus()).append(": ");
                report.append(tag.getLabel() != null ? tag.getLabel() : tag.getId());
                report.append("\n\n");

                report.append("- **Reference**: `")
                    .append(tag.getReference().getFilePath())
                    .append("`\n");

                report.append("- **Type**: ").append(tag.getType()).append("\n");
                report.append("- **Description**: ").append(tag.getDescription()).append("\n");

                if (tag.getExpectedContent() != null && tag.getActualContent() != null) {
                    report.append("\n**Expected**:\n```\n");
                    report.append(tag.getExpectedContent());
                    report.append("\n```\n\n");

                    report.append("**Actual**:\n```\n");
                    report.append(tag.getActualContent());
                    report.append("\n```\n");
                }
            }
        }

        return report.toString();
    }
}
