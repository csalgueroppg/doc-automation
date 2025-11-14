package com.ppg.iicsdoc.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.model.metadata.MetadataTag;
import com.ppg.iicsdoc.model.metadata.MetadataTag.MetadataTagType;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Slf4j
@Component
public class MetadataTagParser {

    /**
     * Pattern for @tagName or @tag-name followed by optional attributes
     * and value
     */
    private static final Pattern TAG_PATTERN = Pattern.compile(
            "@([a-zA-Z][a-zA-Z0-9-_]*)" + // Tag name
                    "(?:\\(([^)]*)\\))?" + // Optional attributes in parentheses
                    "\\s*:?\\s*" + // Optional colon separator
                    "([^\n@]*)", // Value until newline or next tag
            Pattern.MULTILINE);

    /** Pattern for attributes (key=value) */
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "([a-zA-Z][a-zA-Z0-9_-]*)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s,)]+))");

    public List<MetadataTag> parseTags(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        List<MetadataTag> tags = new ArrayList<>();
        Matcher matcher = TAG_PATTERN.matcher(text);

        while (matcher.find()) {
            try {
                String tagName = matcher.group(1);
                String attributesStr = matcher.group(2);
                String value = matcher.group(3);

                Map<String, String> attributes = parseAttributes(attributesStr);
                MetadataTagType type = parseTagType(tagName);

                MetadataTag tag = MetadataTag.builder()
                        .tagName(tagName)
                        .value(value != null ? value.trim() : "")
                        .type(type)
                        .attributes(attributes)
                        .build();

                tags.add(tag);
                log.debug("Parsed tag: @{} = {}", tagName, value);

            } catch (Exception e) {
                log.warn("Failed to parse tag: {}", matcher.group(0), e);
            }
        }

        log.info("Parsed {} metadata tags", tags.size());
        return tags;
    }

    public String extractPlainText(String text) {
        if (text == null) {
            return "";
        }

        return TAG_PATTERN.matcher(text).replaceAll("").trim();
    }

    public MetadataTag getTagByType(List<MetadataTag> tags, MetadataTagType type) {
        return tags.stream()
                .filter(tag -> tag.getType() == type)
                .findFirst()
                .orElse(null);
    }

    public List<MetadataTag> getTagsByType(List<MetadataTag> tags, MetadataTagType type) {
        return tags.stream()
                .filter(tag -> tag.getType() == type)
                .toList();
    }

    public boolean hasTag(List<MetadataTag> tags, MetadataTagType type) {
        return tags.stream().anyMatch(tag -> tag.getType() == type);
    }

    public String getTagValue(List<MetadataTag> tags, MetadataTagType type) {
        MetadataTag tag = getTagByType(tags, type);
        return tag != null ? tag.getValue() : null;
    }

    /**
     * Parses a string of attribute key-value pairs into a map.
     * 
     * <p>
     * Supported formats for values:
     * </p>
     * 
     * <ul>
     * <li>Unquoted: {@code key=value}</li>
     * <li>Double-quoted: {@code key="value"}</li>
     * <li>Single-quoted: {@code key='value'}</li>
     * </ul>
     * 
     * <p>
     * Keys must start with a letter and may contain letters, digits,
     * underscores, or hyphens. Values are trimmed and null-safe.
     * </p>
     * 
     * @param attributesStr the raw attribute string (e.g. {@code language=java},
     *                      {@code version=1.0.0})
     * @return a map of parsed attributes; empty if input is null or blank input
     */
    private Map<String, String> parseAttributes(String attributesStr) {
        if (attributesStr == null || attributesStr.isBlank()) {
            return Map.of();
        }

        Map<String, String> attributes = new HashMap<>();
        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributesStr);

        while (matcher.find()) {
            try {
                String key = matcher.group(1);
            String value = matcher.group(2) != null
                    ? matcher.group(2)
                    : matcher.group(3) != null
                            ? matcher.group(3)
                            : matcher.group(4);

                if (key != null && value != null) {
                    attributes.put(key, value);
                } else {
                    log.warn("Skipping malformed attribute, key=%s, value=%s%n", 
                        key, value);
                }
            } catch (Exception e) {
                log.error("Error parsing attribute: " + e.getMessage());
            }
        }

        return attributes;
    }

    private MetadataTagType parseTagType(String tagName) {
        String normalized = tagName.toUpperCase().replace('-', '_');

        try {
            return MetadataTagType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return MetadataTagType.CUSTOM;
        }
    }
}
