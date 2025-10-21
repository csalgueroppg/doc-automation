package com.ppg.iicsdoc.model.domain;

/**
 * Defines supported transformation types used in data integration flows.
 * 
 * <p>
 * Each type represents a specific operation applied to data during processing,
 * and is typically used to classify transformation components in metadata.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
public enum TransformationType {
    
    /** Applies expression or formulas to manipulate field values. */
    EXPRESSION,

    /** Filters records based on specified conditions. */
    FILTER,

    /** Aggregates data using group-by and summary operations. */
    AGGREGATOR,

    /** Joins data from multiple sources based on matching keys. */
    JOINER,

    /** Performs lookups against reference data or external sources. */
    LOOKUP,

    /** Routes records to different paths based on conditions. */
    ROUTER,

    /** Sorts records based on one or more fields. */
    SORTER,

    /** Combines multiple data streams into a single unified output. */
    UNION,

    /** Any other transformation type not explicitly listed. */
    OTHER
}
