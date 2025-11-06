package com.ppg.iicsdoc.model.tags;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of a tag verification process for a document or
 * set of tags. A {@code TagVerificationResult} summarizes the number of
 * tags in various states, including valid, outdated, missing, and error
 * conditions.
 * 
 * <p>
 * This class is typically used to report the outcome of automated or 
 * manual verification routines that check whether tagged content remains
 * consistent with its expected state.
 * </p>
 * 
 * <h2>Example Usage</h2>
 * 
 * <p>
 * The following example demonstrates how a verification result might be
 * used to assess the health of a tagged document:
 * </p>
 * 
 * <pre>{@code
 * TagVerificationResult result = TagVerificationResult.builder()
 *  .totalTags(10)
 *  .validTags(8)
 *  .outdatedTags(1)
 *  .missingTags(1)
 *  .errorTags(0)
 *  .problematicTags(List.of(tag1, tag2))
 *  .build();
 * 
 * if (!result.isAllValid()) {
 *  System.out.println("Some tags require attention.");
 * }
 * 
 * double successRate = result.getSuccessRate(); // 0.8 
 * }</pre>
 * 
 * <p>
 * This result can be stored alongside a {@link TaggedDocument} to track
 * verification history and trigger alerts or reviews when necessary.
 * </p>
 * 
 * @see Tag
 * @see TaggedDocument
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagVerificationResult {
    
    /** Total number of tags evaluated during verification. */
    private int totalTags;

    /** Number of tags that passed verification successfully. */
    private int validTags;

    /** Number of tags whose content has changed from the expected state. */
    private int outdatedTags;

    /** Number of tags whose referenced content could not be found. */
    private int missingTags;

    /** Number of tags that encountered an error during verification. */
    private int errorTags;

    /** List of tags that require attention due to verification issues. */
    private List<Tag> problematicTags;

    /**
     * Checks whether all tags are valid.
     * 
     * @return {@code true} if all tags are valid and at least one was 
     *         verified; {@code false} otherwise
     */
    public boolean isAllValid() {
        return totalTags > 0 && validTags == totalTags;
    }

    /**
     * Returns the success rate of the verification process.
     * 
     * @return a value between {@code 0.0} and {@code 1.0} representing
     *         the proportion of valid tags; returns {@code 0.0} if not
     *         were verified.
     */
    public double getSuccessRate() {
        if (totalTags == 0) {
            return 0.0;
        }

        return (double) validTags / totalTags;
    }
}
