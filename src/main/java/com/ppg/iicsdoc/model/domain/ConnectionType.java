package com.ppg.iicsdoc.model.domain;

/**
 * Defines supported connection types for external systems and
 * data sources.
 * 
 * <p>
 * This enum is used to classify the nature of a connection, whether it's a
 * web service, file system, cloud storage, or messaging platform.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
public enum ConnectionType {
    /** RESTful web service connection. */
    REST,

    /** SOAP-based web service connection. */
    SOAP,

    /** Relational or NoSQL database connection. */
    DATABASE,

    /** Local or remote file system access. */
    FILE,

    /** FTP (File Transfer Protocol) connection. */
    FTP,

    /** SFTP (Secure File Transfer Protocol) connection. */
    SFTP,

    /** Amazon S3 cloud storage connection. */
    S3,

    /** Apache Kafka messaging system connection. */
    KAFKA,

    /** Any other connection type not explicitly listed. */
    OTHER
}
