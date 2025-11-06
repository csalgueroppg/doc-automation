package com.ppg.iicsdoc.model.tags;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a document that contains content and associated tags
 * for verification, synchronization, or documentation purposes. A
 * {@code TaggedDocument} may include multiple {@link Tag} instances,
 * each referencing specific elements within the content.
 * 
 * <p>
 * Tagged documents are commonly used in systems that validate code
 * snippets, configuration references, or transformation definitions.
 * Each tag can be verified against the document's content to ensure
 * consistency and accuracy.
 * </p>
 * 
 * <h2>Example Usage</h2>
 * 
 * <p>
 * The following example demonstrates how a tagging system might
 * associate tags with a document and verify their status.
 * </p>
 * 
 * <pre>{@code
 * TaggedDocument doc = TaggedDocument.builder()
 *         .documentId("user-service.md")
 *         .content(Files.readString(Path.of("docs/user-service.md")))
 *         .build();
 * 
 * TagReference ref = TagReference.lines("src/UserService.java", 42, 55);
 * Tag tag = Tag.builder()
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
 * doc.add(tag);
 * 
 * if (!doc.allTagsValid()) {
 *     doc.getProblematicTags().forEach(t -> System.out.println(
 *             "Tag " + t.getId() + " needs review"));
 * }
 * }</pre>
 * 
 * <p>
 * Tagged documents may also include a {@link TagVerificationResult} to
 * summarize the outcome of batch verification process.
 * </p>
 * 
 * @see Tag
 * @see TagReference
 * @see TagVerificationResult
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaggedDocument {

    /** Unique identifier for the document, such as a filename or logical ID. */
    private String documentId;

    /** The full content of the document being tagged and verified. */
    private String content;

    /** A list of tags associated with this document. */
    private List<Tag> tags;

    /** The result of the most recent tag verification process. */
    private TagVerificationResult verificationResult;

    /**
     * Adds a new tag to the document.
     * 
     * @param tag the {@link Tag} to add
     */
    public void addTag(Tag tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }

        tags.add(tag);
    }

    /**
     * Returns a list of tags that require attention due to
     * verification issues.
     * 
     * @return a list of problematic tags, or an empty list if none
     */
    public List<Tag> getProblematicTags() {
        if (tags == null) {
            return List.of();
        }

        return tags.stream()
                .filter(Tag::needsAttention)
                .toList();
    }

    /**
     * Checks whether all tags in the document are valid.
     * 
     * @return {@code true} if all tags are valid or if no tags are present;
     *         {@code false} otherwise
     */
    public boolean allTagsValid() {
        if (tags == null || tags.isEmpty()) {
            return true;
        }

        return tags.stream().allMatch(Tag::isValid);
    }
}
