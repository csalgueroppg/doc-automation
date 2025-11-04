package com.ppg.iicsdoc.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.model.tags.Tag;
import com.ppg.iicsdoc.model.tags.TagReference;
import com.ppg.iicsdoc.model.tags.TaggedDocument;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TagParser {

    private static final Pattern TAG_PATTERN = Pattern.compile(
        "\\[iics:(\\w+)\\]\\(([^)\"]+)(?:\\s+\"([^\"]+)\"?\\)",
        Pattern.MULTILINE);

    public TaggedDocument parse(String documentId, String content) {
        log.debug("Parsing tags from document: {}", documentId);

        List<Tag> tags = new ArrayList<>();
        Matcher matcher = TAG_PATTERN.matcher(content);

        while (matcher.find()) {
            try {
                String type = matcher.group(1);
                String reference = matcher.group(2);
                String label = matcher.group(3);

                Tag tag = parseTag(type, reference, label);
                if (tag != null) {
                    tags.add(tag);
                }
            } catch (Exception e) {
                log.warn("Failed to parse tag: {}", matcher.group(0), e);
            }
        }

        log.info("Found {} tags in document", tags.size());
        return TaggedDocument.builder()
            .documentId(documentId)
            .content(content)
            .tags(tags)
            .build();
    }

    private Tag parseTag(String type, String reference, String label) {
        Tag.TagType tagType = parseTagType(type);
        TagReference tagRef = parseReference(reference, tagType);

        if (tagRef == null) {
            log.warn("Could not parse reference: {}", reference);
            return null;
        }

        return Tag.builder()
            .id(generateTagId())
            .type(tagType)
            .label(label)
            .reference(tagRef)
            .status(Tag.TagStatus.NOT_VERIFIED)
            .build();
    }

    private Tag.TagType parseTagType(String type) {
        return switch (type.toLowerCase()) {
            case "file" -> Tag.TagType.FILE_REFERENCE;
            case "lines", "line" -> Tag.TagType.LINE_REFERENCE;
            case "xpath" -> Tag.TagType.XPATH_REFERENCE;
            case "snippet" -> Tag.TagType.CODE_SNIPPET;
            case "element", "elem" -> Tag.TagType.CONNECTION_REF;
            case "connection", "conn" -> Tag.TagType.CONNECTION_REF;
            case "transformation", "trans" -> Tag.TagType.TRANSFORMATION_REF;
            case "dataflow", "flow" -> Tag.TagType.DATA_FLOW_REF;
            default -> Tag.TagType.CUSTOM;
        };
    }

    private TagReference parseReference(String reference, Tag.TagType type) {
        String[] parts = reference.split("#", 2);
        String filePath = parts[0];
        String fragment = parts.length > 1 ? parts[1] : null;

        if (fragment == null) {
            return TagReference.file(filePath);
        }

        if (fragment.matches("[L]?\\d+-[L]?\\d+")) {
            return parseLineReference(filePath, fragment);
        }

        if (fragment.startsWith("//")) {
            return TagReference.xpath(filePath, filePath);
        }

        return TagReference.elementId(filePath, fragment);
    }

    private TagReference parseLineReference(String filePath, String fragment) {
        String cleaned = fragment.replaceAll("[L]", "");
        String[] parts = cleaned.split("-");

        try {
            int startLine = Integer.parseInt(parts[0]);
            int endLine = Integer.parseInt(parts[1]);

            return TagReference.lines(filePath, startLine, endLine);
        } catch (NumberFormatException e) {
            log.warn("Invalid line range: {}", fragment);
            return null;
        }
    }

    private String generateTagId() {
        return "tag_" + System.nanoTime();
    }

    private Map<String, Integer> extractTagPosition(String content) {
        Map<String, Integer> positions = new HashMap<>();
        Matcher matcher = TAG_PATTERN.matcher(content);

        while (matcher.find()) {
            positions.put(matcher.group(0), matcher.start());
        }

        return positions;
    }
}
