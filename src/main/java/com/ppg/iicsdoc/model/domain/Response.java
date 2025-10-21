package com.ppg.iicsdoc.model.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a response definition for an API endpoint.
 * 
 * <p>
 * This model captures metadata about the response code, description, and
 * schema, and is typically used in OpenAPI specification, documentation 
 * generation, or dynamic client scaffolding.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Data
@Builder
public class Response {

   /** The HTTP status code of the response (e.g., {@code "200"}) */
   private String code;

   /** A human-readable description of the response's meaning or purpose. */
   private String description;

   /** The type of the response schema (e.g., {@code "object"}). */
   private String schemaType;

   /** 
    * The type of items contained in the schema, if {@code schemaType}
    * is {@code "array"}.
    */
   private String schemaItems; 
}
