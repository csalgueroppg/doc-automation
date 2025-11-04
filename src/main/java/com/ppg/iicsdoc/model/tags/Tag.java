package com.ppg.iicsdoc.model.tags;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    private String id;
    private TagType type;
    private String label;
    private String description;
    private TagReference reference;
    private TagStatus status;
    private LocalDateTime lastVerified;
    private String expectedContent;
    private String actualContent;
    private Map<String, String> metadata;

    /** Tag types */
    public enum TagType {
        /** Points to a file */
        FILE_REFERENCE,

        /** Points to a specific line(s) */
        LINE_REFERENCE,

        /** Points to XML element via XPath */
        XPATH_REFERENCE,

        /** Embedded code snippet that auto-updates */
        CODE_SNIPPET,

        /** References a connection definition */
        CONNECTION_REF,

        /** References a transformation */
        TRANSFORMATION_REF,

        /** References data flow section */
        DATA_FLOW_REF,

        /** Custom data type */
        CUSTOM
    };

    /** Tag Status */
    public enum TagStatus {
        /** Content matches expected */
        VALID,

        /** Content has changed */
        OUTDATED,

        /** Referenced content not found */
        MISSING,

        /** Error verifying */
        ERROR,

        /** Not yet verified */
        NOT_VERIFIED
    };

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
