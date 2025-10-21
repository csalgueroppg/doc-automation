package com.ppg.iicsdoc.model.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Parameter {
    private String name;
    private ParameterLocation in;
    private String type;
    private boolean required;
    private String description;
    private String defaultValue;
}
