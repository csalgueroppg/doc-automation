package com.ppg.iicsdoc.model.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a connection configuration to an external data
 * source or service.
 * 
 * <p>
 * This model encapsulates metadata required to establish and 
 * identify a connection including its type, location, and
 * authentication method.
 * </p>
 * 
 * <p>
 * It is typically used in scenarios such as parser configuration,
 * deployment, or integration with third-party APIs and databases.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Data
@Builder
public class Connection {

    /** A unique identifier for the connection. */
    private String id;

    /** A human-readable name for the connection. */
    private String name;

    /** The type of connection (e.g., HTTP, JDBC, FTP). */
    private ConnectionType type;

    /** The full URL used to connect to the target service or endpoint. */
    private String url;

    /** The host name or IP address of the target system. */
    private String host;

    /** The name of the database, if applicable. */
    private String database;

    /** The authentication mechanism used to access the connection. */
    private AuthenticationType authenticationType;
}
