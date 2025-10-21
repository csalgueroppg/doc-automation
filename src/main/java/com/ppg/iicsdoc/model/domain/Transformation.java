package com.ppg.iicsdoc.model.domain;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a transformation step within a data integration or processing
 * pipeline.
 * 
 * <p>
 * This model captures metadata about the transformation logic, including its
 * type, expression, condition, and the fields it consumes and produces. It is
 * to define how data is manipulated between source and target systems.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Data
@Builder
public class Transformation {

    /** A unique identifier for the transformation. */
    private String id;

    /** A human-readable name for the transformation. */
    private String name;

    /** The type of transformation (e.g., expression, filter). */
    private TransformationType type;

    /** The transformation expression or formula applied to input fields. */
    private String expression;

    /** A conditional clause that determines when the transformation is applied. */
    private String condition;

    /** A list of input fields consumed by the transformation. */
    private List<Field> inputFields;

    /** A list of output fields produced by the transformation. */
    private List<Field> outputFields;
}
