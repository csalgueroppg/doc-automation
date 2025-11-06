package com.ppg.iicsdoc.model.tags;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Represents a tagged reference to a specific element such as a file,
 * line range, XML node, code snippet, or data transformation. A tag
 * may include both expected and actual content, along with metadata
 * and verification status.
 *
 * <p>
 * Tags can be used to track dependencies, validate documentation links,
 * or synchronize external artifacts such as code examples, configuration
 * references, or transformation definitions. A tagging system can periodically
 * verify these references to ensure they remain accurate and up to date.
 * </p>
 *
 * <h2>Example Usage</h2>
 *
 * <p>
 * The following example demonstrates how a tagging system might define
 * and verify a tag that references a code snippet in a source file:
 * </p>
 * 
 * <pre>{@code
 * TagReference ref = TagReference.builder()
 *         .build();
 * 
 * Tag userServiceTag = Tag.builder()
 *         .id("userService.createUser")
 *         .type(Tag.TagType.LINE_REFERENCE)
 *         .label("User creation method")
 *         .description("Ensures that the UserService#createUser method is consistent")
 *         .reference(ref)
 *         .expectedContent("public User createUser(UserRequest request)")
 *         .status(Tag.TagStatus.NOT_VERIFIED)
 *         .lastVerified(LocalDateTime.now())
 *         .build();
 * 
 * // Later, a verifier might check if the content still matches
 * boolean isUpToDate = userServiceTag.isValid();
 * if (userServiceTag.needsAttention()) {
 *     System.out.println("Tag " + userServiceTag.getId() + " requires review");
 * }
 * }</pre>
 * 
 * <p>
 * In a typical tagging system, tags like these would be stored in metadata
 * files, associated with documentation or configuration, and periodically
 * validated against their referenced targets (e.g., source files, XML
 * documents, or connection definitions).
 * </p>
 *
 * @see Tag.TagType
 * @see Tag.TagStatus
 * @see TagReference
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    /** Unique identifier for the tag. */
    private String id;

    /** The type og tag, indicating what kind of element it references. */
    private TagType type;

    /** A short, human-readable label for this tag. */
    private String label;

    /** A detailed description of the tag's purpose or content. */
    private String description;

    /** Reference information for the target resource or content. */
    private TagReference reference;

    /** Current verification status of the tag. */
    private TagStatus status;

    /** Timestamp of the last successful verification. */
    private LocalDateTime lastVerified;

    /** The expected content that should be referenced or validated. */
    private String expectedContent;

    /** The actual content currently found at the tag's reference. */
    private String actualContent;

    /** Additional metadata describing or qualifying this tag. */
    private Map<String, String> metadata;

    // Nested Types
    /**
     * Defines the possible types of tags, describing what kind of
     * element a tag refers to.
     */
    public enum TagType {

        /** References an entire file. */
        FILE_REFERENCE,

        /** References a specific line or range of lines in a file. */
        LINE_REFERENCE,

        /** References an XML element or node via XPath. */
        XPATH_REFERENCE,

        /** Represents an embedded code snippet that updates automatically. */
        CODE_SNIPPET,

        /** References a defined connection configuration. */
        CONNECTION_REF,

        /** References a data transformation. */
        TRANSFORMATION_REF,

        /** References a section within a data flow. */
        DATA_FLOW_REF,

        /** Represents a custom or user-defined tag type. */
        CUSTOM
    }

    /**
     * Represents the verification state of a tag.
     */
    public enum TagStatus {

        /** The tag's actual content matches the expected content. */
        VALID,

        /** The tag's referenced content has changed from its expected state. */
        OUTDATED,

        /** The referenced content could not be found. */
        MISSING,

        /** An error occurred during verification. */
        ERROR,

        /** The tag has not yet been verified. */
        NOT_VERIFIED
    }

    /**
     * Checks if a tag has a status of valid.
     * 
     * @return {@code true} if the tag's status is valid, {@code false} otherwise
     */
    public boolean isValid() {
        return status == TagStatus.VALID;
    }

    /**
     * Requires the user to verify the contents of the tag.
     * 
     * @return {@code true} if the tag's status matches {@link TagStatus#OUTDATED},
     *         {@link TagStatus#MISSING}, or {@link TagStatus#ERROR}
     */
    public boolean needsAttention() {
        return status == TagStatus.OUTDATED ||
                status == TagStatus.MISSING ||
                status == TagStatus.ERROR;
    }
}