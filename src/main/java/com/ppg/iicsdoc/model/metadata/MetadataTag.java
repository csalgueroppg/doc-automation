package com.ppg.iicsdoc.model.metadata;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a structured tag within metadata fields similar to JSDoc/OpenAPI
 * Spec annotations.
 * 
 * <p>
 * </p>
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * {@code
 * &#64;purpose Integration with Salesforce
 * &#64;system-requirements Database access, API credentials
 * &#64;performance SLA 99.9%, max 5s response time 
 * }</pre>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataTag {

    /** Tag name */
    private String tagName;

    /** Value associated with the tag */
    private String value;

    /** Type of tag */
    private MetadataTagType type;

    /** Additional attributes found in the tag's content */
    private Map<String, String> attributes;

    public enum MetadataTagType {
        PURPOSE,
        DESCRIPTION,
        SUMMARY,
        SYSTEM_REQUIREMENTS,
        PREREQUISITES,
        DEPENDENCIES,
        PERFORMANCE,
        SLA,
        SCALING,
        CAPACITY,
        SECURITY,
        COMPLIANCE,
        DATA_CLASSIFICATION,
        MONITORING,
        ALERTING,
        ERROR_HANDLING,
        RETRY_POLICY,
        BUSINESS_OWNER,
        STAKEHOLDERS,
        BUSINESS_IMPACT,
        EXAMPLE,
        SEE_ALSO,
        DEPRECATED,
        SINCE,
        VERSION,
        INPUT_FORMAT,
        OUTPUT_FORMAT,
        DATA_SOURCE,
        DATA_TARGET,
        CUSTOM
    }

    /**
     * Checks if the requested tag has a status of {@code Deprecated}
     * 
     * @return {@code true} if the tag is deprecated, {@code false otherwise}
     */
    public boolean isDeprecated() {
        return type == MetadataTagType.DEPRECATED;
    }
}
