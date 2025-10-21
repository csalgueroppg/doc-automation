package com.ppg.iicsdoc.model.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a logical data flow from a source to a target, optionally
 * passing through transformations.
 * 
 * <p>
 * This model is used to define how data is ingested, processed, and 
 * delivered across systems. It encapsulates the origin of the data, 
 * references to transformation steps, and the destination.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Data
@Builder
public class DataFlow {

    /** The source from which data is retrieved. */
    private DataSource source;

    /** 
     * A list of transformations identifiers applied to the data.
     * These may refer to transformation steps, rules, or processors.
     */
    private List<String> transformationRefs;

    /** The target to which the transformed data is delivered. */
    private DataTarget target;
}
