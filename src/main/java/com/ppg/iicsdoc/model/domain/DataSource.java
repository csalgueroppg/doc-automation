package com.ppg.iicsdoc.model.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the origin of a data flow.
 * 
 * <p>
 * This model defines the reference to a connection and the specific entry
 * (e.g., table, file, topic) being accessed. It is typically used to configure
 * data ingestion or extraction steps in a pipeline.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Data
@Builder
public class DataSource {

    /**
     * The identifier of the connection used to access the source.
     * This should match a {@link com.ppg.iicsdoc.model.domain.Connection}
     * reference.
     */
    private String connectionRef;

    /** The name of the entity being accessed (e.g., table name, file name) */
    private String entity;
}
