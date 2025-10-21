package com.ppg.iicsdoc.model.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the destination of a data flow.
 * 
 * <p>
 * This model defines the reference to a connection and the specific entity
 * (e.g., table, file, topic) where the data should be written or delivered.
 * It is typically used to configure data output or publishing steps in a
 * pipeline.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Data
@Builder
public class DataTarget {

    /**
     * The identifier of the connection used to access the target.
     * This should match a {@link com.example.model.domain.Connection} reference.
     */
    private String connectionRef;

    /** The name of the entity being written to (e.g., table name, file name) */
    private String entity;
}