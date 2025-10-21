package com.ppg.iicsdoc.model.domain;

/**
 * Defines supported process types in an IICS metadata context.
 * 
 * <p>
 * Each type corresponds to a category of integration supported by 
 * Informatica Cloud, and includes a human-readable display name for the
 * documentation or UI purposes.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
public enum ProcessType {
    /** 
     * CAI 
     * Typically used for real-time APIs, service orchestration, and
     * event-driven flows.
     */
    CAI("Cloud Application Integration"),

    /** 
     * CDI
     * Typically used for batch data processing, ETL pipeline, and 
     * data synchronization.
     */
    CDI("Cloud Data Integration");

    /** Human-readable display name for the process types. */
    private final String displayName;

    /**
     * Creates a new Process Type
     * 
     * @param displayName human-readable display namet o be set
     */
    ProcessType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable display name of the process type.
     * 
     * @return the display name (e.g., "Cloud Application Integration")
     */
    public String getDisplayName() {
        return this.displayName;
    }
}
