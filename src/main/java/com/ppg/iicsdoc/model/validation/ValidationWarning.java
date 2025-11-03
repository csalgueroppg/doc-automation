package com.ppg.iicsdoc.model.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a possible warning or non-critical issue from the
 * schema validation service.
 * 
 * <p>
 * Contains warning metadata such as: code, human-friendly message, and XML
 * metadata such as xPath information, line number, and column number.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationWarning {

    /** Associated warning code */
    private String code;

    /** Human-friendly message associated with the warning */
    private String message;
    
    /** XPath where the warnings was generated */
    private String xPath;

    /** XML line number where the warning occurred */
    private int lineNumber;

    /** XML column number where the warning occurred */
    private String recommendation;
}
