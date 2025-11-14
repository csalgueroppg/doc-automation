package com.ppg.iicsdoc.tags;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.metadata.MetadataTag;
import com.ppg.iicsdoc.model.tags.Tag;
import com.ppg.iicsdoc.model.tags.TagReference;
import com.ppg.iicsdoc.model.tags.TaggedDocument;

import lombok.extern.slf4j.Slf4j;

/**
 * Parses tagged references from document content using a predefined
 * pattern. A {@code TagParser} extracts {@link Tag} instances from
 * inline annotations in markdown or text files, and constructs a
 * {@link TaggedDocument} with associated metadata.
 * 
 * <p>
 * Tags are expected to follow a specific syntax, such as:
 * 
 * <pre>
 * [iicstype
 * </pre>
 * 
 * where:
 * <ul>
 * <li>{@code type} is that tag type (e.g., file, lines, xpath)</li>
 * <li>{@code path#fragment} is the reference to the target resource</li>
 * <li>{@code "Label"} is an optional human-readable label</li>
 * </ul>
 * </p>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * TagParser parser = new TagParser();
 * String content = Files.readString(Path.of("docs/user-service.md"));
 * 
 * TaggedDocument doc = parser.parse("user-service.md", content);
 * doc.getTags().forEach(tag -> {
 *     System.out.println("Found tag: " + tag.getId() + " (" + tag.getLabel() + ")");
 * });
 * }</pre>
 * 
 * <p>
 * The parser supports multiple tag types and reference formats, including:
 * <ul>
 * <li>Line ranges (e.g., {@code file.java#L10-L20})</li>
 * <li>XPath expressions (e.g., {@code config.xml#//settings/option})</li>
 * <li>Element IDs (e.g., {@code config.xml#timeout-option})</li>
 * </ul>
 * </p>
 * 
 * @see Tag
 * @see TagReference
 * @see TaggedDocument
 * @see Tag.TagType
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-04
 */
@Slf4j
@Component
public class TagParser {

    /** Pattern used to identify tag annotations in document content. */
    private static final Pattern TAG_PATTERN = Pattern.compile(
            "\\[iics:(\\w+)\\]\\(([^)\"]+)(?:\\s+\"([^\"]+)\"?)?\\)",
            Pattern.MULTILINE);

    /**  */
    private static final SecureRandom secureRandom = new SecureRandom();

    /**  */
    private static final String ALPHANUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Parses the given document content and extracts all recognized tags.
     * 
     * @param documentId the identifier of the document (e.g., filename)
     * @param content    the full content of the document
     * @return a {@link TaggedDocument} containing parsed tags
     */
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
                log.warn("Failed to parse tag: {}",
                        matcher.group(0), e);
            }
        }

        log.info("Found {} tags in document", tags.size());
        return TaggedDocument.builder()
                .documentId(documentId)
                .content(content)
                .tags(tags)
                .build();
    }

    /**
     * Extracts the position of each tag in the document content.
     * 
     * @param content the document content
     * @return a map of strings to their start positions
     */
    public Map<String, Integer> extractTagPosition(String content) {
        Map<String, Integer> positions = new HashMap<>();
        Matcher matcher = TAG_PATTERN.matcher(content);

        while (matcher.find()) {
            positions.put(matcher.group(0), matcher.start());
        }

        return positions;
    }

    /**
     * Constructs a {@link Tag} from parsed components.
     * 
     * @param type      the tag type string
     * @param reference the reference string (e.g., file path with fragment)
     * @param label     the optional label
     * @return a {@link Tag} instance or {@code null} if parsing fails.
     */
    private Tag parseTag(String type, String reference, String label) {
        Tag.TagType tagType = parseTagType(type);
        TagReference tagRef = parseReference(reference, tagType);

        if (tagRef == null) {
            log.warn("Could not parse reference: {}", reference);
            return null;
        }

        return Tag.builder()
                .id(generateTagId("tag", tagType, true))
                .type(tagType)
                .label(label)
                .reference(tagRef)
                .status(Tag.TagStatus.NOT_VERIFIED)
                .build();
    }

    /**
     * Maps a string to a {@link Tag.TagType} enum value.
     * 
     * @param type the tag type string
     * @return the corresponding {@link Tag.TagType}
     */
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

    /**
     * Parses a reference string into a {@link TagReference} based on tag type.
     * 
     * @param reference the reference string (e.g., {@code file.java#L10-L20})
     * @param type      the tag type
     * @return a {@link TagReference} or {@code null} if parsing fails
     */
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
            return TagReference.xpath(filePath, fragment);
        }

        return TagReference.elementId(filePath, fragment);
    }

    /**
     * Parses a line range fragment into a {@link TagReference}.
     * 
     * @param filePath the file path
     * @param fragment the line range fragment (e.g., {@code L10-L20})
     * @return a {@link TagReference} or {@code null} if parsing fails
     */
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

    /**
     * Generates a cryptographically secure tag identifier using random
     * alphanumeric characters.
     * 
     * <p>
     * This method uses {@link java.security.SecureRandom} to ensure
     * unpredictability, making it suitable for secure tagging in
     * distributed or multi-agent systems.
     * </p>
     * 
     * <p>
     * The tag ID includes a customizable prefix, a timestamp, and a
     * random suffix. This format improves traceability and uniqueness across
     * distributed systems.
     * </p>
     * 
     * <p>
     * Example output: {@code tag_LINE_REFERENCE_20251105_ab12Xy9Kq3Lm}
     * </p>
     * 
     * @param prefix           optional prefix (e.g., "tag", "ref", etc.)
     * @param tagType          optional tag type to include in the ID
     * @param includeTimestamp whether to include a date-based timestamp
     * @return a secure, traceable identifier
     */
    private String generateTagId(
            String prefix,
            Tag.TagType tagType,
            boolean includeTimestamp) {
        StringBuilder id = new StringBuilder();
        if (prefix != null && !prefix.isBlank()) {
            id.append(prefix).append("_");
        }

        if (tagType != null) {
            id.append(tagType.name()).append("_");
        }

        if (includeTimestamp) {
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            id.append(date).append("_");
        }

        for (int i = 0; i < 16; i++) {
            id.append(ALPHANUM.charAt(secureRandom.nextInt(ALPHANUM.length())));
        }

        return id.toString();
    }
}
